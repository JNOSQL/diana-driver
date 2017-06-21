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
package org.jnosql.diana.hbase.column;


import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.jnosql.diana.api.Condition;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.ValueWriter;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnCondition;
import org.jnosql.diana.api.column.ColumnDeleteQuery;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.ColumnFamilyManager;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.writer.ValueWriterDecorator;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.jnosql.diana.api.Condition.AND;
import static org.jnosql.diana.api.Condition.EQUALS;
import static org.jnosql.diana.api.Condition.IN;

/**
 * The Hbase implementation to {@link ColumnFamilyManager}.
 * It does not support TTL methods
 * <p>{@link HBaseColumnFamilyManager#save(ColumnEntity, Duration)}</p>
 */
public class HBaseColumnFamilyManager implements ColumnFamilyManager {

    private static final String KEY_REQUIRED_ERROR = "\"To save an entity is necessary to have an row, a Column that has a blank name. Documents.of(\\\"\\\", keyValue);\"";

    private final Connection connection;
    private final Table table;
    private final ValueWriter writerField = ValueWriterDecorator.getInstance();


    HBaseColumnFamilyManager(Connection connection, Table table) {
        this.connection = connection;
        this.table = table;
    }

    @Override
    public ColumnEntity insert(ColumnEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        String family = entity.getName();
        List<Column> columns = entity.getColumns();
        if (columns.isEmpty()) {
            return entity;
        }
        Column columnID = entity.find(HBaseUtils.KEY_COLUMN).orElseThrow(() -> new DianaHBaseException(KEY_REQUIRED_ERROR));

        Put put = new Put(Bytes.toBytes(valueToString(columnID.getValue())));
        columns.stream().filter(Predicate.isEqual(columnID).negate()).forEach(column -> {
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column.getName()), Bytes
                    .toBytes(valueToString(column.getValue())));
        });
        try {
            table.put(put);
        } catch (IOException e) {
            throw new DianaHBaseException("An error happened when try to save an entity", e);
        }
        return entity;
    }

    @Override
    public ColumnEntity update(ColumnEntity entity) throws NullPointerException {
        return insert(entity);
    }

    @Override
    public ColumnEntity insert(ColumnEntity entity, Duration ttl) throws NullPointerException {
        throw new UnsupportedOperationException("There is not support to save async");
    }


    @Override
    public void delete(ColumnDeleteQuery query) {
        Objects.requireNonNull(query, "query is required");
        ColumnCondition condition = query.getCondition()
                .orElseThrow(() -> new IllegalArgumentException("Condition is required"));
        checkedCondition(condition);
        List<String> values = new ArrayList<>();

        convert(condition, values);
        List<Delete> deletes = values
                .stream()
                .map(String::getBytes)
                .map(Delete::new)
                .collect(toList());
        try {
            table.delete(deletes);
        } catch (IOException e) {
            throw new DianaHBaseException("An error when try to delete columns", e);
        }

    }


    @Override
    public List<ColumnEntity> select(ColumnQuery query) {
        Objects.requireNonNull(query, "query is required");
        ColumnCondition condition = query.getCondition()
                .orElseThrow(() -> new IllegalArgumentException("Condition is required"));
        checkedCondition(condition);
        return Stream.of(findById(condition)).map(EntityUnit::new).filter(EntityUnit::isNotEmpty).map(EntityUnit::toEntity).collect(toList());
    }


    @Override
    public void close() {
        try {
            connection.close();
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String valueToString(Value value) {
        Object object = value.get();
        if (writerField.isCompatible(object.getClass())) {
            return writerField.write(object).toString();
        } else {
            return object.toString();
        }
    }

    private Result[] findById(ColumnCondition condition) {
        List<String> values = new ArrayList<>();
        convert(condition, values);

        List<Get> gets = values.stream()
                .map(String::getBytes)
                .map(Get::new).collect(toList());
        try {
            return table.get(gets);
        } catch (IOException e) {
            throw new DianaHBaseException("An error when try to find by id", e);
        }
    }

    private Map<String, List<Column>> toMap(Result result) {
        Map<String, List<Column>> columnsByKey = new HashMap<>();
        for (Cell cell : result.rawCells()) {
            String key = new String(CellUtil.cloneRow(cell));
            String name = new String(CellUtil.cloneQualifier(cell));
            String value = new String(CellUtil.cloneValue(cell));
            List<Column> columns = columnsByKey.getOrDefault(key, new ArrayList<>());
            columns.add(Column.of(name, value));
            columnsByKey.put(key, columns);
        }
        return columnsByKey;
    }


    private void convert(ColumnCondition columnCondition, List<String> values) {
        Condition condition = columnCondition.getCondition();

        if (AND.equals(condition)) {
            columnCondition.getColumn().get(new TypeReference<List<ColumnCondition>>() {
            }).forEach(c -> convert(c, values));
        } else if (IN.equals(condition)) {
            values.addAll(columnCondition.getColumn().get(new TypeReference<List<String>>() {
            }));
        } else if (EQUALS.equals(condition)) {
            values.add(valueToString(columnCondition.getColumn().getValue()));
        }


    }

    private void checkedCondition(ColumnCondition columnCondition) {

        Condition condition = columnCondition.getCondition();
        if (AND.equals(condition)) {
            List<ColumnCondition> columnConditions = columnCondition.getColumn().get(new TypeReference<List<ColumnCondition>>() {
            });
            for (ColumnCondition cc : columnConditions) {
                checkedCondition(cc);
            }
            return;

        }
        if (!EQUALS.equals(condition) && !IN.equals(condition)) {
            throw new UnsupportedOperationException("Hbase does not support the following condition: %s just AND, EQUAL and IN ");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HBaseColumnFamilyManager{");
        sb.append("connection=").append(connection);
        sb.append('}');
        return sb.toString();
    }


}
