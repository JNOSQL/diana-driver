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


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsyncFactory;
import org.jnosql.diana.api.column.ColumnFamilyManagerFactory;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * The Cassandra implementation to {@link ColumnFamilyManagerFactory}
 */
public class CassandraDocumentEntityManagerFactory implements ColumnFamilyManagerFactory<CassandraColumnFamilyManager>
        , ColumnFamilyManagerAsyncFactory<CassandraColumnFamilyManagerAsync> {

    private final Cluster cluster;

    private final Executor executor;

    CassandraDocumentEntityManagerFactory(final Cluster cluster, List<String> queries, Executor executor) {
        this.cluster = cluster;
        this.executor = executor;
        runIniticialQuery(queries);
    }

    public void runIniticialQuery(List<String> queries) {
        Session session = cluster.connect();
        queries.forEach(session::execute);
        session.close();
    }

    @Override
    public CassandraColumnFamilyManager get(String database) {
        return new CassandraColumnFamilyManager(cluster.connect(database), executor, database);
    }

    @Override
    public CassandraColumnFamilyManagerAsync getAsync(String database) throws UnsupportedOperationException, NullPointerException {
        return new CassandraColumnFamilyManagerAsync(cluster.connect(database), executor, database);
    }

    @Override
    public void close() {
        cluster.close();
    }

    Cluster getCluster() {
        return cluster;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CassandraDocumentEntityManagerFactory{");
        sb.append("cluster=").append(cluster);
        sb.append(", executor=").append(executor);
        sb.append('}');
        return sb.toString();
    }
}
