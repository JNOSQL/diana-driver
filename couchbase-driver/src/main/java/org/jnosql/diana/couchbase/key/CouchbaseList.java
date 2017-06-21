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
import com.couchbase.client.java.datastructures.collections.CouchbaseArrayList;
import org.jnosql.diana.driver.value.JSONValueProvider;
import org.jnosql.diana.driver.value.JSONValueProviderService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * The couchbase implementation to {@link List}
 * that avoid null items, so if any null object will launch {@link NullPointerException}
 * This class is a wrapper to {@link CouchbaseArrayList}. Once they only can save primitive type,
 * objects are converted to Json {@link String} using {@link JSONValueProvider#toJson(Object)}
 *
 * @param <T> the object to be stored.
 */
public class CouchbaseList<T> implements List<T> {

    private static final JSONValueProvider PROVDER = JSONValueProviderService.getProvider();

    private final String bucketName;
    private final Class<T> clazz;
    private final CouchbaseArrayList<String> arrayList;

    CouchbaseList(Bucket bucket, String bucketName, Class<T> clazz) {
        this.bucketName = bucketName + ":list";
        this.clazz = clazz;
        this.arrayList = new CouchbaseArrayList(this.bucketName, bucket);
    }

    @Override
    public int size() {
        return arrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return arrayList.isEmpty();
    }



    @Override
    public boolean add(T t) {
        requireNonNull(t, "object is required");
        return arrayList.add(PROVDER.toJson(t));
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        requireNonNull(collection, "collection is required");
        collection.forEach(this::add);
        return true;
    }


    @Override
    public void clear() {
        arrayList.clear();

    }

    @Override
    public T get(int i) {
        return PROVDER.of(arrayList.get(i)).get(clazz);
    }

    @Override
    public T set(int i, T t) {
        requireNonNull(t, "object is required");
        String json = arrayList.set(i, PROVDER.toJson(t));
        if (Objects.nonNull(json)) {
            return PROVDER.of(json).get(clazz);
        }
        return null;
    }


    @Override
    public T remove(int i) {
        String json = arrayList.remove(i);
        if (Objects.nonNull(json)) {
            return PROVDER.of(json).get(clazz);
        }
        return null;
    }


    @Override
    public Iterator<T> iterator() {
        return StreamSupport.stream(arrayList.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .collect(Collectors.toList()).iterator();
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(arrayList.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .toArray(size -> new Object[size]);
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        requireNonNull(t1s, "arrys is required");
        return StreamSupport.stream(arrayList.spliterator(), false)
                .map(s -> PROVDER.of(s).get(clazz))
                .toArray(size -> t1s);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return arrayList.retainAll(collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return arrayList.removeAll(collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }

    @Override
    public void add(int i, T t) {
        requireNonNull(t, "object is required");
        arrayList.add(i, PROVDER.toJson(t));
    }

    @Override
    public int indexOf(Object o) {
        requireNonNull(o, "object is required");
        return arrayList.indexOf(PROVDER.toJson(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        requireNonNull(o, "object is required");
        return arrayList.lastIndexOf(PROVDER.toJson(o));
    }

    @Override
    public ListIterator<T> listIterator() {
        return StreamSupport.stream(spliteratorUnknownSize(arrayList.listIterator(), Spliterator.ORDERED),
                false).map(PROVDER::of)
                .map(v -> v.get(clazz))
                .collect(Collectors.toList())
                .listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return StreamSupport.stream(spliteratorUnknownSize(arrayList.listIterator(i), Spliterator.ORDERED),
                false).map(PROVDER::of)
                .map(v -> v.get(clazz))
                .collect(Collectors.toList())
                .listIterator();
    }

    @Override
    public List<T> subList(int i, int i1) {
        return arrayList.subList(i, i1).stream().map(PROVDER::of)
                .map(v -> v.get(clazz)).collect(Collectors.toList());
    }

    @Override
    public boolean remove(Object o) {
        requireNonNull(o, "object is required");
        return arrayList.remove(PROVDER.toJson(o));
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        requireNonNull(collection, "collection is required");
        return arrayList.containsAll(collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }
    @Override
    public boolean contains(Object o) {
        requireNonNull(o, "object is required");
        return arrayList.contains(PROVDER.toJson(o));
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        requireNonNull(collection, "collection is required");
        return arrayList.addAll(i, collection.stream().map(PROVDER::toJson).collect(Collectors.toList()));
    }
}
