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

package org.jnosql.diana.hazelcast.key;

import org.jnosql.diana.api.key.BucketManagerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class KeyValueConfigurationTest {

    private HazelCastKeyValueConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new HazelCastKeyValueConfiguration();
    }

    @Test
    public void shouldCreateKeyValueFactory() {
        Map<String, String> map = new HashMap<>();
        BucketManagerFactory managerFactory = configuration.get(map);
        assertNotNull(managerFactory);
    }

    @Test
    public void shouldCreateKeyValueFactoryFromFile() {
        BucketManagerFactory managerFactory = configuration.get();
        assertNotNull(managerFactory);
    }

}