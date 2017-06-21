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
package org.jnosql.diana.cassandra.column;

import com.datastax.driver.core.ConsistencyLevel;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.Columns;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.jnosql.diana.cassandra.column.Constants.COLUMN_FAMILY;
import static org.jnosql.diana.cassandra.column.Constants.KEY_SPACE;


public class CassandraColumnFamilyManagerAsyncTest {

    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.ONE;
    private CassandraColumnFamilyManagerAsync columnEntityManager;

    @Before
    public void setUp() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        CassandraConfiguration cassandraConfiguration = new CassandraConfiguration();
        CassandraDocumentEntityManagerFactory entityManagerFactory = cassandraConfiguration.get();
        columnEntityManager = entityManagerFactory.getAsync(KEY_SPACE);
    }

    @Test
    public void shouldInsertJustKeyAsync() {
        Column key = Columns.of("id", 10L);
        ColumnEntity columnEntity = ColumnEntity.of(COLUMN_FAMILY);
        columnEntity.add(key);
        columnEntityManager.insert(columnEntity);
    }

    @Test
    public void shouldInsertColumnsAsync() {
        ColumnEntity columnEntity = getColumnFamily();
        columnEntityManager.insert(columnEntity);
    }

    @Test
    public void shouldInsertColumnsAsyncWithConsistenceLevel() {
        ColumnEntity columnEntity = getColumnFamily();
        columnEntityManager.save(columnEntity, CONSISTENCY_LEVEL);
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
    public void end(){
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}