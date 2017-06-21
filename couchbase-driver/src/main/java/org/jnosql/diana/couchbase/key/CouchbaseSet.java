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
package org.jnosql.diana.couchbase.key;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.datastructures.collections.CouchbaseArraySet;
import org.jnosql.diana.driver.value.JSONValueProvider;
import org.jnosql.diana.driver.value.JSONValueProviderService;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;


/**
 * The couchbase implementation to {@link Set}
 * that avoid null items, so if any null object will launch {@link NullPointerException}.
 * This class is a wrapper to {@link CouchbaseArraySet}. Once they only can save primitive type,
 * objects are converted to Json {@link String} using {@link JSONValueProvider#toJson(Object)}
 * @param <T> the object to be stored.
 */
public class CouchbaseSet<T> implements Set<T> {

    private static final JSONValueProvider PROVDER = JSONValueProviderService.getProvider();

    private final String bucketName;
    private final Class<T> clazz;
    private final CouchbaseArraySet<String> arraySet;

    CouchbaseSet(Bucket bucket, String bucketName, Class<T> clazz) {
        this.bucketName = bucketName + ":set";
        this.clazz = clazz;
        this.arraySet = new CouchbaseArraySet(this.bucketName, bucket);
    }

    @Override
    public int size() {
        return arraySet.size();
    }

    @Override
    public boolean isEmpty() {
        return arraySet.isEmpty();
    }

    @Override
    public boolean add(T t) {
        requireNonNull(t, "object is required");
        return arraySet.add(PROVDER.toJson(t));
    }

    @Override
    public boolean remove(Object o) {
        requireNonNull(o, "object is required");
        return arraySet.remove(PROVDER.toJson(o));
    }

    @Override
    public boolean contains(Object o) {
        requireNonNull(o, "object is required");
        return arraySet.contains(PROVDER.toJson(o));
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        requireNonNull(collection, "collection is required");
        collection.forEach(this::add);
        return true;
    }

    @Override
    public void clear() {
        arraySet.clear();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return collection.stream().allMatch(this::contains);
    }

    @Override
    public Iterator<T> iterator() {
        return StreamSupport.stream(arraySet.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .collect(Collectors.toList()).iterator();
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(arraySet.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .toArray(size -> new Object[size]);
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        requireNonNull(t1s, "arrys is required");
        return StreamSupport.stream(arraySet.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .toArray(size -> t1s);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return arraySet.retainAll(collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return arraySet.removeAll(collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }


}
