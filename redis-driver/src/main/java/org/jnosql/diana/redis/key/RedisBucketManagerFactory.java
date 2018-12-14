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

package org.jnosql.diana.redis.key;

import org.jnosql.diana.api.key.BucketManagerFactory;

/**
 * The redis implementation to {@link BucketManagerFactory} where returns {@link RedisBucketManager}
 */
public interface RedisBucketManagerFactory extends BucketManagerFactory<RedisBucketManager> {


    /**
     * Creates a {@link SortedSet} from key
     *
     * @param key the key
     * @return the SortedSet from key
     * @throws NullPointerException when key is null
     */
    SortedSet getSortedSet(String key) throws NullPointerException;
    /**
     * Creates {@link Counter}
     *
     * @param key the key to counter
     * @return a counter instance from key
     * @throws NullPointerException when key is null
     */
    Counter getCounter(String key) throws NullPointerException;

}
