/*
 * Copyright 2017 Otavio Santana and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.jnosql.diana.redis.key;


import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.KeyValueEntity;
import org.jnosql.diana.driver.value.JSONValueProvider;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.jnosql.diana.redis.key.RedisUtils.createKeyWithNameSpace;

/**
 * The redis implementation to {@link BucketManager}
 */
public class RedisKeyValueEntityManager implements BucketManager {

    private final String nameSpace;
    private final JSONValueProvider provider;

    private final Jedis jedis;

    RedisKeyValueEntityManager(String nameSpace, JSONValueProvider provider, Jedis jedis) {
        this.nameSpace = nameSpace;
        this.provider = provider;
        this.jedis = jedis;
    }

    @Override
    public <K, V> void put(K key, V value) throws NullPointerException {
        Objects.requireNonNull(value, "Value is required");
        Objects.requireNonNull(key, "key is required");
        String valideKey = createKeyWithNameSpace(key.toString(), nameSpace);
        jedis.set(valideKey, provider.toJson(value));
    }

    @Override
    public <K> void put(KeyValueEntity<K> entity) throws NullPointerException {
        put(entity.getKey(), entity.getValue().get());
    }

    @Override
    public <K> void put(KeyValueEntity<K> entity, Duration ttl) throws NullPointerException, UnsupportedOperationException {
        put(entity);
        String valideKey = createKeyWithNameSpace(entity.getKey().toString(), nameSpace);
        jedis.expire(valideKey, (int) ttl.getSeconds());
    }

    @Override
    public <K> void put(Iterable<KeyValueEntity<K>> entities) throws NullPointerException {
        StreamSupport.stream(entities.spliterator(), false).forEach(this::put);
    }

    @Override
    public <K> void put(Iterable<KeyValueEntity<K>> entities, Duration ttl) throws NullPointerException, UnsupportedOperationException {
        StreamSupport.stream(entities.spliterator(), false).forEach(this::put);
        StreamSupport.stream(entities.spliterator(), false).map(KeyValueEntity::getKey)
                .map(k -> createKeyWithNameSpace(k.toString(), nameSpace))
                .forEach(k -> jedis.expire(k, (int) ttl.getSeconds()));
    }

    @Override
    public <K> Optional<Value> get(K key) throws NullPointerException {
        String value = jedis.get(createKeyWithNameSpace(key.toString(), nameSpace));
        if (value != null && !value.isEmpty()) {
            return Optional.of(provider.of(value));
        }
        return Optional.empty();
    }

    @Override
    public <K> Iterable<Value> get(Iterable<K> keys) throws NullPointerException {
        return StreamSupport.stream(keys.spliterator(), false)
                .map(k -> jedis.get(createKeyWithNameSpace(k.toString(), nameSpace)))
                .filter(value -> value != null && !value.isEmpty())
                .map(v -> provider.of(v)).collect(toList());
    }

    @Override
    public <K> void remove(K key) {
        jedis.del(createKeyWithNameSpace(key.toString(), nameSpace));
    }

    @Override
    public <K> void remove(Iterable<K> keys) {
        StreamSupport.stream(keys.spliterator(), false).forEach(this::remove);
    }

    @Override
    public void close() {
        jedis.close();
    }
}
