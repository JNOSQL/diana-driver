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

import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.driver.value.JSONValueProvider;
import org.jnosql.diana.driver.value.JSONValueProviderService;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * The redis implementation to {@link BucketManagerFactory} where returns {@link RedisKeyValueEntityManager}
 */
public class RedisKeyValueEntityManagerFactory implements BucketManagerFactory<RedisKeyValueEntityManager> {

    private static final JSONValueProvider PROVDER = JSONValueProviderService.getProvider();

    private final JedisPool jedisPool;

    RedisKeyValueEntityManagerFactory(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    @Override
    public RedisKeyValueEntityManager getBucketManager(String bucketName) {
        Objects.requireNonNull(bucketName, "bucket name is required");

        return new RedisKeyValueEntityManager(bucketName, PROVDER, jedisPool.getResource());
    }

    @Override
    public <T> List<T> getList(String bucketName, Class<T> clazz) {
        Objects.requireNonNull(bucketName, "bucket name is required");
        Objects.requireNonNull(clazz, "Class type is required");
        return new RedisList<T>(jedisPool.getResource(), clazz, bucketName);
    }

    @Override
    public <T> Set<T> getSet(String bucketName, Class<T> clazz) {
        Objects.requireNonNull(bucketName, "bucket name is required");
        Objects.requireNonNull(clazz, "Class type is required");
        return new RedisSet<T>(jedisPool.getResource(), clazz, bucketName);
    }

    @Override
    public <T> Queue<T> getQueue(String bucketName, Class<T> clazz) {
        Objects.requireNonNull(bucketName, "bucket name is required");
        Objects.requireNonNull(clazz, "Class type is required");
        return new RedisQueue<T>(jedisPool.getResource(), clazz, bucketName);
    }

    @Override
    public <K, V> Map<K, V> getMap(String bucketName, Class<K> keyValue, Class<V> valueValue) {
        Objects.requireNonNull(bucketName, "bucket name is required");
        Objects.requireNonNull(valueValue, "Class type is required");
        return new RedisMap<>(jedisPool.getResource(), keyValue, valueValue, bucketName);
    }

    @Override
    public void close() {
        jedisPool.close();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RedisKeyValueEntityManagerFactory{");
        sb.append("jedisPool=").append(jedisPool);
        sb.append('}');
        return sb.toString();
    }
}
