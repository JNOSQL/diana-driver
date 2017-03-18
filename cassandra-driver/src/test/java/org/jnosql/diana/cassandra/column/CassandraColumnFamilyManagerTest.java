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

package org.jnosql.diana.cassandra.column;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.Session;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.hamcrest.Matchers;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnCondition;
import org.jnosql.diana.api.column.ColumnDeleteQuery;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.column.Columns;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jnosql.diana.cassandra.column.Constants.COLUMN_FAMILY;
import static org.jnosql.diana.cassandra.column.Constants.KEY_SPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class CassandraColumnFamilyManagerTest {

    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.ONE;
    private CassandraColumnFamilyManager columnEntityManager;

    @Before
    public void setUp() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        CassandraConfiguration cassandraConfiguration = new CassandraConfiguration();
        CassandraDocumentEntityManagerFactory entityManagerFactory = cassandraConfiguration.get();
        columnEntityManager = entityManagerFactory.get(KEY_SPACE);
    }


    @Test
    public void shouldClose() throws Exception {
        columnEntityManager.close();
        CassandraColumnFamilyManager cassandraColumnFamilyManager = CassandraColumnFamilyManager.class.cast(columnEntityManager);
        Session session = cassandraColumnFamilyManager.getSession();
        assertTrue(session.isClosed());
    }

    @Test
    public void shouldInsertJustKey() {
        Column key = Columns.of("id", 10L);
        ColumnEntity columnEntity = ColumnEntity.of(COLUMN_FAMILY);
        columnEntity.add(key);
        columnEntityManager.save(columnEntity);
    }


    @Test
    public void shouldInsertColumns() {
        ColumnEntity columnEntity = getColumnFamily();
        columnEntityManager.save(columnEntity);
    }

    @Test
    public void shouldInsertColumnsWithConsistencyLevel() {
        ColumnEntity columnEntity = getColumnFamily();
        columnEntityManager.save(columnEntity, CONSISTENCY_LEVEL);
    }


    @Test
    public void shouldFindById() {

        columnEntityManager.save(getColumnFamily());
        ColumnQuery query = ColumnQuery.of(COLUMN_FAMILY).and(ColumnCondition.eq(Columns.of("id", 10L)));
        List<ColumnEntity> columnEntity = columnEntityManager.find(query);
        assertFalse(columnEntity.isEmpty());
        List<Column> columns = columnEntity.get(0).getColumns();
        assertThat(columns.stream().map(Column::getName).collect(toList()), containsInAnyOrder("name", "version", "options", "id"));
        assertThat(columns.stream().map(Column::getValue).map(Value::get).collect(toList()), containsInAnyOrder("Cassandra", 3.2, asList(1, 2, 3), 10L));

    }

    @Test
    public void shouldFindByIdWithConsistenceLevel() {

        columnEntityManager.save(getColumnFamily());
        ColumnQuery query = ColumnQuery.of(COLUMN_FAMILY).and(ColumnCondition.eq(Columns.of("id", 10L)));
        List<ColumnEntity> columnEntity = columnEntityManager.find(query, CONSISTENCY_LEVEL);
        assertFalse(columnEntity.isEmpty());
        List<Column> columns = columnEntity.get(0).getColumns();
        assertThat(columns.stream().map(Column::getName).collect(toList()), containsInAnyOrder("name", "version", "options", "id"));
        assertThat(columns.stream().map(Column::getValue).map(Value::get).collect(toList()), containsInAnyOrder("Cassandra", 3.2, asList(1, 2, 3), 10L));

    }

    @Test
    public void shouldRunNativeQuery() {
        columnEntityManager.save(getColumnFamily());
        List<ColumnEntity> entities = columnEntityManager.cql("select * from newKeySpace.newColumnFamily where id=10;");
        assertFalse(entities.isEmpty());
        List<Column> columns = entities.get(0).getColumns();
        assertThat(columns.stream().map(Column::getName).collect(toList()), containsInAnyOrder("name", "version", "options", "id"));
        assertThat(columns.stream().map(Column::getValue).map(Value::get).collect(toList()), containsInAnyOrder("Cassandra", 3.2, asList(1, 2, 3), 10L));
    }

    @Test
    public void shouldPrepareStatment() {
        columnEntityManager.save(getColumnFamily());
        CassandraPrepareStatment preparedStatement = columnEntityManager.nativeQueryPrepare("select * from newKeySpace.newColumnFamily where id=?");
        preparedStatement.bind(10L);
        List<ColumnEntity> entities = preparedStatement.executeQuery();
        List<Column> columns = entities.get(0).getColumns();
        assertThat(columns.stream().map(Column::getName).collect(toList()), containsInAnyOrder("name", "version", "options", "id"));
        assertThat(columns.stream().map(Column::getValue).map(Value::get).collect(toList()), containsInAnyOrder("Cassandra", 3.2, asList(1, 2, 3), 10L));
    }

    @Test
    public void shouldDeleteColumnFamily() {
        columnEntityManager.save(getColumnFamily());
        ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 10L)));
        ColumnQuery query = ColumnQuery.of(COLUMN_FAMILY).and(ColumnCondition.eq(Columns.of("id", 10L)));
        columnEntityManager.delete(ColumnDeleteQuery.of(query.getColumnFamily(), query.getCondition().get()));
        List<ColumnEntity> entities = columnEntityManager.cql("select * from newKeySpace.newColumnFamily where id=10;");
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void shouldDeleteColumnFamilyWithConsistencyLevel() {
        columnEntityManager.save(getColumnFamily());
        ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 10L)));
        ColumnQuery query = ColumnQuery.of(COLUMN_FAMILY).and(ColumnCondition.eq(Columns.of("id", 10L)));
        columnEntityManager.delete(ColumnDeleteQuery.of(query.getColumnFamily(), query.getCondition().get()), CONSISTENCY_LEVEL);
        List<ColumnEntity> entities = columnEntityManager.cql("select * from newKeySpace.newColumnFamily where id=10;");
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void shouldLimitResult() {
        getEntities().forEach(columnEntityManager::save);
        ColumnQuery query = ColumnQuery.of(COLUMN_FAMILY);
        query.and(ColumnCondition.in(Column.of("id", asList(1L, 2L, 3L))));
        query.setLimit(2L);
        List<ColumnEntity> columnFamilyEntities = columnEntityManager.find(query);
        assertEquals(Integer.valueOf(2), Integer.valueOf(columnFamilyEntities.size()));
    }

    private List<ColumnEntity> getEntities() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "Cassandra");
        fields.put("version", 3.2);
        fields.put("options", asList(1, 2, 3));
        List<Column> columns = Columns.of(fields);
        ColumnEntity entity = ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 1L)));
        ColumnEntity entity1 = ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 2L)));
        ColumnEntity entity2 = ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 3L)));
        columns.forEach(entity::add);
        columns.forEach(entity1::add);
        columns.forEach(entity2::add);
        return asList(entity, entity1, entity2);
    }

    @Test
    public void shouldSupportUDT() {
        ColumnEntity entity = ColumnEntity.of("users");
        entity.add(Column.of("nickname", "ada"));
        List<Column> columns = new ArrayList<>();
        columns.add(Column.of("firstname", "Ada"));
        columns.add(Column.of("lastname", "Lovelace"));
        UDT udt = UDT.builder().withName("name")
                .withTypeName("fullname")
                .addAll(columns).build();
        entity.add(udt);
        columnEntityManager.save(entity);
        ColumnQuery query = ColumnQuery.of("users");
        ColumnEntity columnEntity = columnEntityManager.singleResult(query).get();
        Column column = columnEntity.find("name").get();
        udt = UDT.class.cast(column);
        List<Column> udtColumns = udt.getColumns();
        Assert.assertEquals("name", udt.getName());
        Assert.assertEquals("fullname", udt.getUserType());
        assertThat(udtColumns, Matchers.containsInAnyOrder(Column.of("firstname", "Ada"),
                Column.of("lastname", "Lovelace")));
    }

    @Test
    public void shouldSupportAnUDTElement() {
        ColumnEntity entity = ColumnEntity.of("users");
        entity.add(Column.of("nickname", "Ioda"));
        List<Column> columns = new ArrayList<>();
        columns.add(Column.of("firstname", "Ioda"));
        UDT udt = UDT.builder().withName("name")
                .withTypeName("fullname")
                .addAll(columns).build();
        entity.add(udt);
        columnEntityManager.save(entity);
        ColumnQuery query = ColumnQuery.of("users");
        ColumnEntity columnEntity = columnEntityManager.singleResult(query).get();
        Column column = columnEntity.find("name").get();
        udt = UDT.class.cast(column);
        List<Column> udtColumns = udt.getColumns();
        Assert.assertEquals("name", udt.getName());
        Assert.assertEquals("fullname", udt.getUserType());
        assertThat(udtColumns, Matchers.containsInAnyOrder(Column.of("firstname", "Ioda")));
    }


    @Test
    public void shouldSupportDate() {
        ColumnEntity entity = ColumnEntity.of("history");
        entity.add(Column.of("name", "World war II"));
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date dateEnd = Date.from(java.time.LocalDate.of(1945, Month.SEPTEMBER, 2).atStartOfDay(defaultZoneId).toInstant());
        Calendar dataStart = Calendar.getInstance();
        entity.add(Column.of("dataStart", LocalDate.fromYearMonthDay(1939,9, 1)));
        entity.add(Column.of("dateEnd", dateEnd));
        columnEntityManager.save(entity);
        ColumnQuery query = ColumnQuery.of("history");
        query.and(ColumnCondition.eq(Column.of("name", "World war II")));
        ColumnEntity entity1 = columnEntityManager.singleResult(query).get();
        Assert.assertNotNull(entity1);

    }


    private ColumnEntity getColumnFamily() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "Cassandra");
        fields.put("version", 3.2);
        fields.put("options", asList(1, 2, 3));
        List<Column> columns = Columns.of(fields);
        ColumnEntity columnFamily = ColumnEntity.of(COLUMN_FAMILY, singletonList(Columns.of("id", 10L)));
        columns.forEach(columnFamily::add);
        return columnFamily;
    }


    @After
    public void end() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}