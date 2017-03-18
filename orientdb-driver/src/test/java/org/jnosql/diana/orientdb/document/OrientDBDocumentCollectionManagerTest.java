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
package org.jnosql.diana.orientdb.document;

import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCondition;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.Documents;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jnosql.diana.orientdb.document.DocumentConfigurationUtils.get;
import static org.jnosql.diana.orientdb.document.OrientDBConverter.RID_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class OrientDBDocumentCollectionManagerTest {


    public static final String COLLECTION_NAME = "person";

    private OrientDBDocumentCollectionManager entityManager;

    @Before
    public void setUp() {
        entityManager = get().get("database");
        DocumentEntity documentEntity = getEntity();
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("name");
        query.and(DocumentCondition.eq(id.get()));
        try {
            entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));
        } catch (Exception e) {

        }

    }

    @Test
    public void shouldSave() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.save(entity);
        assertNotNull(documentEntity);
        Optional<Document> document = documentEntity.find(RID_FIELD);
        assertTrue(document.isPresent());

    }

    @Test
    public void shouldUpdateSave() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.save(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        DocumentEntity updated = entityManager.update(entity);
        assertEquals(newField, updated.find("newField").get());
    }

    @Test
    public void shouldRemoveEntity() {
        DocumentEntity documentEntity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("name");
        query.and(DocumentCondition.eq(id.get()));
        entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));
        assertTrue(entityManager.find(query).isEmpty());
    }

    @Test
    public void shouldFindDocument() {
        DocumentEntity entity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = entity.find("name");
        query.and(DocumentCondition.eq(id.get()));
        List<DocumentEntity> entities = entityManager.find(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }


    @Test
    public void shouldSaveSubDocument() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Document.of("mobile", "1231231")));
        DocumentEntity entitySaved = entityManager.save(entity);
        Document id = entitySaved.find("name").get();
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        query.and(DocumentCondition.eq(id));
        DocumentEntity entityFound = entityManager.find(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, contains(Document.of("mobile", "1231231")));
    }

    @Test
    public void shouldSaveSubDocument2() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Arrays.asList(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231"))));
        DocumentEntity entitySaved = entityManager.save(entity);
        Document id = entitySaved.find("name").get();
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        query.and(DocumentCondition.eq(id));
        DocumentEntity entityFound = entityManager.find(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, containsInAnyOrder(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231")));
    }

    @Test
    public void shouldQueryAnd() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("age", 24));
        entityManager.save(entity);

        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);

        query.and(DocumentCondition.eq(Document.of("name", "Poliana"))
                .and(DocumentCondition.gte(Document.of("age", 10))));

        assertFalse(entityManager.find(query).isEmpty());

        entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));
        assertTrue(entityManager.find(query).isEmpty());
    }

    @Test
    public void shouldQueryOr() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("age", 24));
        entityManager.save(entity);

        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);

        query.and(DocumentCondition.eq(Document.of("name", "Poliana"))
                .or(DocumentCondition.gte(Document.of("age", 100))));

        assertFalse(entityManager.find(query).isEmpty());

        entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));
        assertTrue(entityManager.find(query).isEmpty());
    }

    @Test
    public void shouldLive() throws InterruptedException {
        List<DocumentEntity> entities = new ArrayList<>();
        Consumer<DocumentEntity> callback = entities::add;

        DocumentEntity entity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = entity.find("name");
        query.and(DocumentCondition.eq(id.get()));
        entityManager.live(query, callback);
        entityManager.save(getEntity());
        Thread.sleep(3_000L);
        assertFalse(entities.isEmpty());
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
}