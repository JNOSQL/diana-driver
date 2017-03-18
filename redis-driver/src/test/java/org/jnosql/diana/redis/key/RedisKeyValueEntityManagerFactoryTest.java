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

import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.api.key.KeyValueConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.junit.Assert.assertNotNull;


public class RedisKeyValueEntityManagerFactoryTest {

    public static final String BUCKET_NAME = "bucketName";
    private BucketManagerFactory managerFactory;

    @Before
    public void setUp() {
        KeyValueConfiguration configuration = new RedisConfiguration();
        managerFactory = configuration.get();
    }

    @Test
    public void shouldCreateKeyValueEntityManager() {
        BucketManager keyValueEntityManager = managerFactory.getBucketManager(BUCKET_NAME);
        assertNotNull(keyValueEntityManager);
    }

    @Test
    public void shouldCreateMap() {
        Map<String, String> map = managerFactory.getMap(BUCKET_NAME, String.class, String.class);
        assertNotNull(map);
    }

    @Test
    public void shouldCreateSet() {
        Set<String> set = managerFactory.getSet(BUCKET_NAME, String.class);
        assertNotNull(set);
    }

    @Test
    public void shouldCreateList() {
        List<String> list = managerFactory.getList(BUCKET_NAME, String.class);
        assertNotNull(list);
    }

    @Test
    public void shouldCreateQueue() {
        Queue<String> queue = managerFactory.getQueue(BUCKET_NAME, String.class);
        assertNotNull(queue);
    }

}