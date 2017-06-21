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

package org.jnosql.diana.mongodb.document;

import org.jnosql.diana.api.TypeReference;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.jnosql.diana.mongodb.document.DocumentConfigurationUtils.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class MongoDBDocumentCollectionManagerTest {

    public static final String COLLECTION_NAME = "person";
    private static DocumentCollectionManager entityManager;

    @BeforeClass
    public static void setUp() throws IOException {
        MongoDbHelper.startMongoDb();
        entityManager = get().get("database");
    }

    @Test
    public void shouldSave() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        assertTrue(documentEntity.getDocuments().stream().map(Document::getName).anyMatch(s -> s.equals("_id")));
    }

    @Test
    public void shouldUpdateSave() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        DocumentEntity updated = entityManager.update(entity);
        assertEquals(newField, updated.find("newField").get());
    }

    @Test
    public void shouldRemoveEntity() {
        DocumentEntity documentEntity = entityManager.insert(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("_id");
        query.and(DocumentCondition.eq(id.get()));
        entityManager.delete(DocumentDeleteQuery.of(query.getCollection(), query.getCondition().get()));
        assertTrue(entityManager.select(query).isEmpty());
    }

    @Test
    public void shouldFindDocument() {
        DocumentEntity entity = entityManager.insert(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = entity.find("_id");
        query.and(DocumentCondition.eq(id.get()));
        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }


    @Test
    public void shouldSaveSubDocument() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Document.of("mobile", "1231231")));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Document id = entitySaved.find("_id").get();
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        query.and(DocumentCondition.eq(id));
        DocumentEntity entityFound = entityManager.select(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, contains(Document.of("mobile", "1231231")));
    }

    @Test
    public void shouldSaveSubDocument2() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Arrays.asList(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231"))));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Document id = entitySaved.find("_id").get();
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        query.and(DocumentCondition.eq(id));
        DocumentEntity entityFound = entityManager.select(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, contains(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231")));
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
    public static void end(){
        MongoDbHelper.stopMongoDb();
    }

    public void init() {
        DocumentEntity oReilly = DocumentEntity.of("oReilly");
        oReilly.add(Document.of("_id", "oreilly"));
        oReilly.add(Document.of("name", "O'Reilly Media"));
        oReilly.add(Document.of("founded", "1980"));
        oReilly.add(Document.of("location", "CA"));
        oReilly.add(Document.of("books", Arrays.asList("123456789", "234567890")));

        DocumentEntity mongoBook = DocumentEntity.of("book");
        mongoBook.add(Document.of("_id", "123456789"));
        mongoBook.add(Document.of("title", "MongoDB: The Definitive Guide"));
        mongoBook.add(Document.of("author", Arrays.asList("ristina Chodorow", "Mike Dirolf")));
        mongoBook.add(Document.of("published_date", "2010-09-24"));
        mongoBook.add(Document.of("pages", 216));
        mongoBook.add(Document.of("language", "English"));
        mongoBook.add(Document.of("publisher_id", "oreilly"));

        DocumentEntity mongoDBDeveloper = DocumentEntity.of("book");
        mongoDBDeveloper.add(Document.of("_id", "234567890"));
        mongoDBDeveloper.add(Document.of("title", "50 Tips and Tricks for MongoDB Developer"));
        mongoDBDeveloper.add(Document.of("author", Arrays.asList("Kristina Chodorow")));
        mongoDBDeveloper.add(Document.of("published_date", "2011-05-06"));
        mongoDBDeveloper.add(Document.of("pages", 68));
        mongoDBDeveloper.add(Document.of("language", "English"));
        mongoDBDeveloper.add(Document.of("publisher_id", "oreilly"));
    }


}