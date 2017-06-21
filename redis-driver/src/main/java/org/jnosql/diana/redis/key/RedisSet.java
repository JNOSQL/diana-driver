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

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jnosql.diana.driver.value.JSONValueProvider;

class RedisSet<T> extends RedisCollection<T> implements Set<T> {

    RedisSet(Jedis jedis, Class<T> clazz, String keyWithNameSpace,JSONValueProvider provider) {
        super(jedis, clazz, keyWithNameSpace,provider);
    }

    @Override
    public boolean add(T e) {
        Objects.requireNonNull(e);
        jedis.sadd(keyWithNameSpace, provider.toJson(e));
        return false;
    }

    @Override
    public void clear() {
        jedis.del(keyWithNameSpace);
    }

    @Override
    public int size() {
        return jedis.scard(keyWithNameSpace).intValue();
    }

    @Override
    protected int indexOf(Object o) {
        Objects.requireNonNull(o);
        String find = provider.toJson(o);
        Set<String> values = jedis.smembers(keyWithNameSpace);
        int index = 0;
        for (String value : values) {
            if (value.contains(find)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    protected T remove(int index) {
        T element = toArrayList().get(index);
        if (element == null) {
            return null;
        }
        remove(element);
        return element;
    }

    @Override
    public boolean remove(Object o) {
        if (!clazz.isInstance(o)) {
            throw new ClassCastException("The object required is " + clazz.getName());
        }
        String find = provider.toJson(o);
        Set<String> values = jedis.smembers(keyWithNameSpace);
        for (String value : values) {
            if (value.contains(find)) {
                jedis.srem(keyWithNameSpace, value);
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<T> toArrayList() {
        Set<String> redisValues = jedis.smembers(keyWithNameSpace);
        List<T> list = new ArrayList<>();
        for (String redisValue : redisValues) {
            list.add(provider.of(redisValue).get(clazz));
        }
        return list;
    }

}
