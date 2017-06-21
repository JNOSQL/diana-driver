/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.diana.riak.key;


import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.UnresolvedConflictException;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.FetchValue.Response;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Namespace;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.KeyValueEntity;
import org.jnosql.diana.driver.value.JSONValueProvider;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.jnosql.diana.riak.key.RiakUtils.createDeleteValue;
import static org.jnosql.diana.riak.key.RiakUtils.createFetchValue;
import static org.jnosql.diana.riak.key.RiakUtils.createStoreValue;

public class RiakKeyValueEntityManager implements BucketManager {


    private final RiakClient client;
    private final JSONValueProvider provider;


    private final Namespace nameSpace;

    RiakKeyValueEntityManager(RiakClient client, JSONValueProvider provider, Namespace nameSpace) {
        this.client = client;
        this.provider = provider;
        this.nameSpace = nameSpace;
    }

    @Override
    public <K, V> void put(K key, V value) throws NullPointerException {
        put(KeyValueEntity.of(key, value));
    }

    @Override
    public <K> void put(KeyValueEntity<K> entity) throws NullPointerException {
        put(entity, Duration.ZERO);
    }

    @Override
    public <K> void put(KeyValueEntity<K> entity, Duration ttl)
            throws NullPointerException, UnsupportedOperationException {

        K key = entity.getKey();
        Value value = entity.getValue();

        StoreValue storeValue = createStoreValue(key, value.get(), nameSpace, ttl);

        try {
            client.execute(storeValue);
        } catch (ExecutionException | InterruptedException e) {
            throw new DianaRiakException(e.getMessage(), e);
        }
    }

    @Override
    public <K> void put(Iterable<KeyValueEntity<K>> entities) throws NullPointerException {
        StreamSupport.stream(entities.spliterator(), false).forEach(this::put);
    }

    @Override
    public <K> void put(Iterable<KeyValueEntity<K>> entities, Duration ttl)
            throws NullPointerException, UnsupportedOperationException {

        StreamSupport.stream(entities.spliterator(), false).forEach(e -> put(e, ttl));
    }

    @Override
    public <K> Optional<Value> get(K key) throws NullPointerException {
        Objects.requireNonNull(key, "key is required");
        if (key.toString().isEmpty()) {
            throw new DianaRiakException("The Key is irregular", new IllegalStateException());
        }

        FetchValue fetchValue = createFetchValue(nameSpace, key);
        try {

            FetchValue.Response response = client.execute(fetchValue);

            String valueFetch = response.getValue(String.class);
            if (Objects.nonNull(valueFetch) && !valueFetch.isEmpty()) {
                return Optional.of(provider.of(valueFetch));
            }

        } catch (ExecutionException | InterruptedException e) {
            throw new DianaRiakException(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public <K> Iterable<Value> get(Iterable<K> keys) throws NullPointerException {

        return StreamSupport.stream(keys.spliterator(), false)
                .map(k -> RiakUtils.createLocation(nameSpace, k))
                .map(l -> new FetchValue.Builder(l).build())
                .map(f ->
                        {
                            try {
                                return client.execute(f);
                            } catch (ExecutionException | InterruptedException e) {
                                throw new DianaRiakException(e.getMessage(), e);
                            }
                        }
                )
                .filter(Response::hasValues)
                .map(r -> {

                    try {
                        return r.getValue(String.class);
                    } catch (UnresolvedConflictException e) {
                        throw new DianaRiakException(e.getMessage(), e);
                    }

                })
                .filter(s -> Objects.nonNull(s) && !s.isEmpty()).map(v -> provider.of(v))
                .collect(toList());
    }


    @Override
    public <K> void remove(K key) throws NullPointerException {

        DeleteValue deleteValue = createDeleteValue(nameSpace, key);

        try {
            client.execute(deleteValue);
        } catch (ExecutionException | InterruptedException e) {
            throw new DianaRiakException(e.getMessage(), e);
        }
    }

    @Override
    public <K> void remove(Iterable<K> keys) throws NullPointerException {
        StreamSupport.stream(keys.spliterator(), false).forEach(this::remove);
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
