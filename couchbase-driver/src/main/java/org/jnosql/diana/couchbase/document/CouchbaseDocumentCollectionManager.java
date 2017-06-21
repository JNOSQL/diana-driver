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
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.ParameterizedN1qlQuery;
import com.couchbase.client.java.query.Statement;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.jnosql.diana.couchbase.document.EntityConverter.ID_FIELD;
import static org.jnosql.diana.couchbase.document.EntityConverter.convert;
import static org.jnosql.diana.couchbase.document.EntityConverter.getPrefix;

/**
 * The couchbase implementation of {@link DocumentCollectionManager}
 */
public class CouchbaseDocumentCollectionManager implements DocumentCollectionManager {

    private final Bucket bucket;
    private final String database;

    CouchbaseDocumentCollectionManager(Bucket bucket, String database) {
        this.bucket = bucket;
        this.database = database;
    }

    @Override
    public DocumentEntity insert(DocumentEntity entity) throws NullPointerException {
        Objects.requireNonNull(entity, "entity is required");
        JsonObject jsonObject = convert(entity);
        Document id = entity.find(ID_FIELD)
                .orElseThrow(() -> new CouchbaseNoKeyFoundException(entity.toString()));

        String prefix = getPrefix(id, entity.getName());
        jsonObject.put(ID_FIELD, prefix);
        bucket.upsert(JsonDocument.create(prefix, jsonObject));
        entity.remove(ID_FIELD);
        entity.add(Document.of(ID_FIELD, prefix));
        return entity;
    }

    @Override
    public DocumentEntity insert(DocumentEntity entity, Duration ttl) {
        Objects.requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl is required");
        JsonObject jsonObject = convert(entity);
        Document id = entity.find(ID_FIELD)
                .orElseThrow(() -> new CouchbaseNoKeyFoundException(entity.toString()));

        String prefix = getPrefix(id, entity.getName());
        bucket.upsert(JsonDocument.create(prefix, jsonObject), ttl.toMillis(), MILLISECONDS);
        return entity;
    }

    @Override
    public DocumentEntity update(DocumentEntity entity) {
        return insert(entity);
    }

    @Override
    public void delete(DocumentDeleteQuery query) {
        QueryConverter.QueryConverterResult delete = QueryConverter.delete(query, database);
        if (nonNull(delete.getStatement())) {
            ParameterizedN1qlQuery n1qlQuery = N1qlQuery.parameterized(delete.getStatement(), delete.getParams());
            N1qlQueryResult result = bucket.query(n1qlQuery);
        }
        if (!delete.getIds().isEmpty()) {
            delete.getIds()
                    .stream()
                    .map(s -> getPrefix(query.getCollection(), s))
                    .forEach(bucket::remove);
        }

    }

    @Override
    public List<DocumentEntity> select(DocumentQuery query) throws NullPointerException {

        QueryConverter.QueryConverterResult select = QueryConverter.select(query, database);
        List<DocumentEntity> entities = new ArrayList<>();
        if (nonNull(select.getStatement())) {
            ParameterizedN1qlQuery n1qlQuery = N1qlQuery.parameterized(select.getStatement(), select.getParams());
            N1qlQueryResult result = bucket.query(n1qlQuery);
            entities.addAll(convert(result, database));
        }
        if (!select.getIds().isEmpty()) {
            entities.addAll(convert(select.getIds(), query.getCollection(), bucket));
        }

        return entities;
    }


    /**
     * Executes the n1qlquery with params and then result que result
     *
     * @param n1qlQuery the query
     * @param params    the params
     * @return the query result
     * @throws NullPointerException when either n1qlQuery or params are null
     */
    public List<DocumentEntity> n1qlQuery(String n1qlQuery, JsonObject params) throws NullPointerException {
        requireNonNull(n1qlQuery, "n1qlQuery is required");
        requireNonNull(params, "params is required");
        N1qlQueryResult result = bucket.query(N1qlQuery.parameterized(n1qlQuery, params));
        return convert(result, database);
    }

    /**
     * Executes the n1qlquery  with params and then result que result
     *
     * @param n1qlQuery the query
     * @param params    the params
     * @return the query result
     * @throws NullPointerException when either n1qlQuery or params are null
     */
    public List<DocumentEntity> n1qlQuery(Statement n1qlQuery, JsonObject params) throws NullPointerException {
        requireNonNull(n1qlQuery, "n1qlQuery is required");
        requireNonNull(params, "params is required");
        N1qlQueryResult result = bucket.query(N1qlQuery.parameterized(n1qlQuery, params));
        return convert(result, database);
    }

    /**
     * Executes the n1qlquery  plain query and then result que result
     *
     * @param n1qlQuery the query
     * @return the query result
     * @throws NullPointerException when either n1qlQuery or params are null
     */
    public List<DocumentEntity> n1qlQuery(String n1qlQuery) throws NullPointerException {
        requireNonNull(n1qlQuery, "n1qlQuery is required");
        N1qlQueryResult result = bucket.query(N1qlQuery.simple(n1qlQuery));
        return convert(result, database);
    }

    /**
     * Executes the n1qlquery  plain query and then result que result
     *
     * @param n1qlQuery the query
     * @return the query result
     * @throws NullPointerException when either n1qlQuery or params are null
     */
    public List<DocumentEntity> n1qlQuery(Statement n1qlQuery) throws NullPointerException {
        requireNonNull(n1qlQuery, "n1qlQuery is required");
        N1qlQueryResult result = bucket.query(N1qlQuery.simple(n1qlQuery));
        return convert(result, database);
    }

    @Override
    public void close() {
        bucket.close();
    }

}
