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
package org.jnosql.diana.mongodb.document;

import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentCondition;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.Documents;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jnosql.diana.mongodb.document.DocumentConfigurationUtils.get;


public class MongoDBDocumentCollectionManagerAsyncTest {

    public static final String COLLECTION_NAME = "person";

    private static DocumentCollectionManager entityManager;

    @BeforeClass
    public static void setUp() throws IOException {
        MongoDbHelper.startMongoDb();
        entityManager = get().get("database");
    }


    @Test
    public void shouldSaveAsync() {
        DocumentEntity entity = getEntity();
        entityManager.save(entity);

    }

    @Test
    public void shouldUpdateAsync() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.save(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        entityManager.update(entity);
    }

    @Test
    public void shouldRemoveEntityAsync() {
        DocumentEntity documentEntity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("_id");
        query.and(DocumentCondition.eq(id.get()));
        entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));

    }

    private DocumentEntity getEntity() {
        DocumentEntity entity = DocumentEntity.of(COLLECTION_NAME);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Poliana");
        map.put("city", "Salvador");
        List<Document> documents = Documents.of(map);
        documents.forEach(entity::add);
        return entity;
    }

    @AfterClass
    public static void end() {
        MongoDbHelper.stopMongoDb();
    }
}