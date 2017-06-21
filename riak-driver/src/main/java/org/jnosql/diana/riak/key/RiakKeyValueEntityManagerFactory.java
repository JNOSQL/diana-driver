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
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Namespace;
import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.driver.value.JSONValueProvider;
import org.jnosql.diana.driver.value.JSONValueProviderService;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * The riak implementation to {@link BucketManagerFactory} that returns {@link RiakKeyValueEntityManager}
 * This implementation just has support to {@link RiakKeyValueEntityManagerFactory#getBucketManager(String)}
 * So, these metdhos will returns {@link UnsupportedOperationException}
 * <p>{@link BucketManagerFactory#getList(String, Class)}</p>
 * <p>{@link BucketManagerFactory#getSet(String, Class)}</p>
 * <p>{@link BucketManagerFactory#getQueue(String, Class)}</p>
 * <p>{@link BucketManagerFactory#getMap(String, Class, Class)}</p>
 */
public class RiakKeyValueEntityManagerFactory implements BucketManagerFactory<RiakKeyValueEntityManager> {

    private static final JSONValueProvider PROVDER = JSONValueProviderService.getProvider();
    private final RiakCluster cluster;

    RiakKeyValueEntityManagerFactory(RiakCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public RiakKeyValueEntityManager getBucketManager(String bucketName) throws UnsupportedOperationException {

        cluster.start();
        RiakClient riakClient = new RiakClient(cluster);
        Namespace quotesBucket = new Namespace(bucketName);

        return new RiakKeyValueEntityManager(riakClient, PROVDER, quotesBucket);
    }

    @Override
    public <T> List<T> getList(String bucketName, Class<T> clazz) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The riak does not support getList method");
    }

    @Override
    public <T> Set<T> getSet(String bucketName, Class<T> clazz) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The riak does not support getSet method");
    }

    @Override
    public <T> Queue<T> getQueue(String bucketName, Class<T> clazz) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The riak does not support getQueue method");
    }

    @Override
    public <K, V> Map<K, V> getMap(String bucketName, Class<K> keyValue, Class<V> valueValue)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The riak does not support getMap method");
    }

    @Override
    public void close() {
        cluster.shutdown();
    }

}
