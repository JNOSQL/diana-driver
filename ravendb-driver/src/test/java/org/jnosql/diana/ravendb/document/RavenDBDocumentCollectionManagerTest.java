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

package org.jnosql.diana.ravendb.document;

import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.Documents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.jnosql.diana.api.document.query.DocumentQueryBuilder.delete;
import static org.jnosql.diana.api.document.query.DocumentQueryBuilder.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RavenDBDocumentCollectionManagerTest {

    public static final String COLLECTION_NAME = "person";
    private static final long TIME_LIMIT = 500L;
    private static final String APPOINTMENT_BOOK = "AppointmentBook";
    private static DocumentCollectionManager entityManager;

    @BeforeAll
    public static void setUp() throws IOException {
        entityManager = DocumentConfigurationUtils.INSTANCE.get().get("database");
    }

    @BeforeEach
    public void before() {
        entityManager.delete(delete().from(COLLECTION_NAME).build());
        entityManager.delete(delete().from(APPOINTMENT_BOOK).build());
    }

    @AfterEach
    public void after() {
        entityManager.delete(delete().from(COLLECTION_NAME).build());
        entityManager.delete(delete().from(APPOINTMENT_BOOK).build());
    }


    @Test
    public void shouldInsert() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        assertTrue(documentEntity.getDocuments().stream().map(Document::getName).anyMatch(s -> s.equals("_id")));
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithTTL() {
        DocumentEntity entity = entityManager.insert(getEntity(), Duration.ofMillis(1));
        Optional<Document> id = entity.find("_id");
        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get().get())
                .build();

    }

    @Test
    public void shouldUpdate() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        DocumentEntity updated = entityManager.update(entity);
        assertEquals(newField, updated.find("newField").get());
    }

    @Test
    public void shouldRemoveEntity() throws InterruptedException {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);

        Optional<Document> id = documentEntity.find("_id");
        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get().get())
                .build();
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("_id")
                .eq(id.get().get())
                .build();

        entityManager.delete(deleteQuery);
        assertTrue(entityManager.select(query).isEmpty());
    }

    @Test
    public void shouldFindDocument() {
        DocumentEntity entity = entityManager.insert(getEntity());
        Optional<Document> id = entity.find("_id");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get().get())
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }

    @Test
    public void shouldRunSingleResult() {
        DocumentEntity entity = entityManager.insert(getEntity());
        Optional<Document> id = entity.find("_id");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get().get())
                .build();

        Optional<DocumentEntity> result = entityManager.singleResult(query);
        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
    }

    @Test
    public void shouldFindDocument2() {
        DocumentEntity entity = entityManager.insert(getEntity());
        Optional<Document> id = entity.find("_id");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("name").eq("Poliana")
                .and("city").eq("Salvador").and("_id").eq(id.get().get())
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }

    @Test
    public void shouldFindDocument3() {
        DocumentEntity entity = entityManager.insert(getEntity());
        Optional<Document> id = entity.find("_id");
        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("name").eq("Poliana")
                .or("city").eq("Salvador")
                .and(id.get().getName()).eq(id.get().get())
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }

    @Test
    public void shouldFindDocumentGreaterThan() throws InterruptedException {
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("type").eq("V").build();
        entityManager.delete(deleteQuery);
        Iterable<DocumentEntity> entitiesSaved = entityManager.insert(getEntitiesWithValues());
        List<DocumentEntity> entities = StreamSupport.stream(entitiesSaved.spliterator(), false).collect(Collectors.toList());

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("age").gt(22)
                .and("type").eq("V")
                .build();

        Thread.sleep(TIME_LIMIT);
        List<DocumentEntity> entitiesFound = entityManager.select(query);
        assertEquals(2, entitiesFound.size());
        assertThat(entitiesFound, not(contains(entities.get(0))));
    }

    @Test
    public void shouldFindDocumentGreaterEqualsThan() throws InterruptedException {
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("type").eq("V").build();
        entityManager.delete(deleteQuery);
        Iterable<DocumentEntity> entitiesSaved = entityManager.insert(getEntitiesWithValues());
        List<DocumentEntity> entities = StreamSupport.stream(entitiesSaved.spliterator(), false).collect(Collectors.toList());

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("age").gte(23)
                .and("type").eq("V")
                .build();

        Thread.sleep(TIME_LIMIT);
        List<DocumentEntity> entitiesFound = entityManager.select(query);
        assertEquals(2, entitiesFound.size());
        assertThat(entitiesFound, not(contains(entities.get(0))));
    }

    @Test
    public void shouldFindDocumentLesserThan() {
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("type").eq("V").build();
        entityManager.delete(deleteQuery);
        Iterable<DocumentEntity> entitiesSaved = entityManager.insert(getEntitiesWithValues());
        List<DocumentEntity> entities = StreamSupport.stream(entitiesSaved.spliterator(), false).collect(Collectors.toList());

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("age").lt(23)
                .and("type").eq("V")
                .build();

        List<DocumentEntity> entitiesFound = entityManager.select(query);
        assertEquals(1, entitiesFound.size());
        assertThat(entitiesFound, contains(entities.get(0)));
    }

    @Test
    public void shouldFindDocumentLesserEqualsThan() throws InterruptedException {
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("type").eq("V").build();
        entityManager.delete(deleteQuery);
        Iterable<DocumentEntity> entitiesSaved = entityManager.insert(getEntitiesWithValues());
        List<DocumentEntity> entities = StreamSupport.stream(entitiesSaved.spliterator(), false).collect(Collectors.toList());

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("age").lte(23)
                .and("type").eq("V")
                .build();

        Thread.sleep(TIME_LIMIT);
        List<DocumentEntity> entitiesFound = entityManager.select(query);
        System.out.println(entitiesFound);
        assertEquals(2, entitiesFound.size());
        assertThat(entitiesFound, contains(entities.get(0), entities.get(2)));
    }


    @Test
    public void shouldFindDocumentIn() throws InterruptedException {
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME).where("type").eq("V").build();
        entityManager.delete(deleteQuery);
        Iterable<DocumentEntity> entitiesSaved = entityManager.insert(getEntitiesWithValues());
        List<DocumentEntity> entities = StreamSupport.stream(entitiesSaved.spliterator(), false).collect(Collectors.toList());

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("location").in(asList("BR", "US"))
                .and("type").eq("V")
                .build();
        Thread.sleep(TIME_LIMIT);
        assertEquals(entities, entityManager.select(query));
    }

    @Test
    public void shouldFindAll() {
        entityManager.insert(getEntity());
        DocumentQuery query = select().from(COLLECTION_NAME).build();
        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
    }


    @Test
    public void shouldSaveSubDocument() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Document.of("mobile", "1231231")));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Document id = entitySaved.find("_id").get();
        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get())
                .build();

        DocumentEntity entityFound = entityManager.select(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, contains(Document.of("mobile", "1231231")));
    }

    @Test
    public void shouldSaveSubDocument2() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", asList(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231"))));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Document id = entitySaved.find("_id").get();

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where(id.getName()).eq(id.get())
                .build();
        DocumentEntity entityFound = entityManager.select(query).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<List<Document>>() {
        });
        assertThat(documents, containsInAnyOrder(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231")));
    }

    @Test
    public void shouldConvertFromListSubdocumentList() {
        DocumentEntity entity = createSubdocumentList();
        entityManager.insert(entity);

    }

    @Test
    public void shouldRetrieveListSubdocumentList() {
        DocumentEntity entity = entityManager.insert(createSubdocumentList());
        Document key = entity.find("_id").get();
        DocumentQuery query = select().from(APPOINTMENT_BOOK)
                .where(key.getName())
                .eq(key.get()).build();

        DocumentEntity documentEntity = entityManager.singleResult(query).get();
        assertNotNull(documentEntity);

        List<List<Document>> contacts = (List<List<Document>>) documentEntity.find("contacts").get().get();

        assertEquals(3, contacts.size());
        assertTrue(contacts.stream().allMatch(d -> d.size() == 3));
    }

    private DocumentEntity createSubdocumentList() {
        DocumentEntity entity = DocumentEntity.of(APPOINTMENT_BOOK);
        entity.add(Document.of("_id", new Random().nextInt()));
        List<List<Document>> documents = new ArrayList<>();

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.EMAIL),
                Document.of("information", "ada@lovelace.com")));

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.MOBILE),
                Document.of("information", "11 1231231 123")));

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.PHONE),
                Document.of("information", "phone")));

        entity.add(Document.of("contacts", documents));
        return entity;
    }


    @Test
    public void shouldCount() {
        DocumentEntity entity = getEntity();
        entityManager.insert(entity);
        assertTrue(entityManager.count(COLLECTION_NAME) > 0);
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

    private List<DocumentEntity> getEntitiesWithValues() {
        DocumentEntity lucas = DocumentEntity.of(COLLECTION_NAME);
        lucas.add(Document.of("name", "Lucas"));
        lucas.add(Document.of("age", 22));
        lucas.add(Document.of("location", "BR"));
        lucas.add(Document.of("type", "V"));

        DocumentEntity otavio = DocumentEntity.of(COLLECTION_NAME);
        otavio.add(Document.of("name", "Otavio"));
        otavio.add(Document.of("age", 25));
        otavio.add(Document.of("location", "BR"));
        otavio.add(Document.of("type", "V"));

        DocumentEntity luna = DocumentEntity.of(COLLECTION_NAME);
        luna.add(Document.of("name", "Luna"));
        luna.add(Document.of("age", 23));
        luna.add(Document.of("location", "US"));
        luna.add(Document.of("type", "V"));

        return asList(lucas, otavio, luna);
    }

}
