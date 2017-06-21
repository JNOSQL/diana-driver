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
package org.jnosql.diana.couchbase.document;


import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.Documents;
import org.jnosql.diana.driver.value.ValueUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

final class EntityConverter {

    static final String ID_FIELD = "_id";
    static final String SPLIT_KEY = ":";
    static final char SPLIT_KEY_CHAR = ':';

    private EntityConverter() {
    }

    static List<DocumentEntity> convert(List<String> ids, String collection, Bucket bucket) {
        return ids
                .stream()
                .map(s -> getPrefix(collection, s))
                .map(bucket::get)
                .filter(Objects::nonNull)
                .map(j -> {
                    List<Document> documents = toDocuments(j.content().toMap());
                    Document id = Document.of(ID_FIELD, j.id());
                    DocumentEntity entity = DocumentEntity.of(j.id().split(SPLIT_KEY)[0], documents);
                    entity.remove(ID_FIELD);
                    entity.add(id);
                    return entity;
                })
                .collect(Collectors.toList());
    }

    static String getPrefix(Document document, String collection) {
        String id = document.get(String.class);
        return getPrefix(collection, id);
    }

    private static List<Document> toDocuments(Map<String, Object> map) {
        List<Document> documents = new ArrayList<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (Map.class.isInstance(value)) {
                documents.add(Document.of(key, toDocuments(Map.class.cast(value))));
            } else if (isADocumentIterable(value)) {
                List<Document> subDocuments = new ArrayList<>();
                for (Object object : Iterable.class.cast(value)) {
                    subDocuments.addAll(toDocuments(Map.class.cast(object)));
                }
                documents.add(Document.of(key, subDocuments));
            } else {
                documents.add(Document.of(key, value));
            }
        }
        return documents;
    }

    private static boolean isADocumentIterable(Object value) {
        return Iterable.class.isInstance(value) &&
                stream(Iterable.class.cast(value).spliterator(), false)
                        .allMatch(d -> Map.class.isInstance(d));
    }

    static String getPrefix(String collection, String id) {
        String[] ids = id.split(SPLIT_KEY);
        if (ids.length == 2 && collection.equals(ids[0])) {
            return id;
        }
        return collection + SPLIT_KEY_CHAR + id;
    }


    static List<DocumentEntity> convert(N1qlQueryResult result, String database) {
        return result.allRows().stream()
                .map(N1qlQueryRow::value)
                .map(JsonObject::toMap)
                .map(m -> (Map<String, Object>) m.get(database))
                .filter(Objects::nonNull)
                .map(Documents::of)
                .map(ds -> {
                    Optional<Document> first = ds.stream().filter(d -> ID_FIELD.equals(d.getName())).findFirst();
                    String collection = first.map(d -> d.get(String.class)).orElse(database).split(SPLIT_KEY)[0];
                    return DocumentEntity.of(collection, ds);
                }).collect(toList());
    }

    static JsonObject convert(DocumentEntity entity) {
        requireNonNull(entity, "entity is required");

        JsonObject jsonObject = JsonObject.create();
        entity.getDocuments().stream()
                .filter(d -> !d.getName().equals(ID_FIELD))
                .forEach(toJsonObject(jsonObject));
        return jsonObject;
    }

    private static Consumer<Document> toJsonObject(JsonObject jsonObject) {
        return d -> {
            Object value = ValueUtil.convert(d.getValue());
            if (Document.class.isInstance(value)) {
                Document document = Document.class.cast(value);
                jsonObject.put(d.getName(), Collections.singletonMap(document.getName(), document.get()));
            } else if (Iterable.class.isInstance(value)) {
                JsonArray jsonArray = JsonArray.create();
                Iterable.class.cast(value).forEach(o -> {
                    if (Document.class.isInstance(o)) {
                        Document document = Document.class.cast(o);
                        jsonArray.add(Collections.singletonMap(document.getName(), document.get()));
                    } else {
                        jsonArray.add(o);
                    }
                });
                jsonObject.put(d.getName(), jsonArray);
            } else {
                jsonObject.put(d.getName(), value);
            }
        };
    }

}
