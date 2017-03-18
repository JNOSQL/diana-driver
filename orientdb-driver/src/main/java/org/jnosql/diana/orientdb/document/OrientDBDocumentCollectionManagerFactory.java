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


import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsyncFactory;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;

import java.io.IOException;

import static java.util.Optional.ofNullable;

/**
 * The OrientDB implementation of {@link DocumentCollectionManagerFactory}
 */
public class OrientDBDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory<OrientDBDocumentCollectionManager>,
        DocumentCollectionManagerAsyncFactory<OrientDBDocumentCollectionManagerAsync> {

    private static final String DATABASE_TYPE = "document";
    private static final String STORAGE_TYPE = "plocal";

    private final String host;
    private final String user;
    private final String password;
    private final String storageType;

    OrientDBDocumentCollectionManagerFactory(String host, String user, String password, String storageType) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.storageType = ofNullable(storageType).orElse(STORAGE_TYPE);
    }

    @Override
    public OrientDBDocumentCollectionManager get(String database) {
        try {
            OServerAdmin serverAdmin = new OServerAdmin(host)
                    .connect(user, password);

            if (!serverAdmin.existsDatabase(database, storageType)) {
                serverAdmin.createDatabase(database, DATABASE_TYPE, storageType);
            }
            OPartitionedDatabasePool pool = new OPartitionedDatabasePool("remote:" + host + '/' + database, user, password);
            return new OrientDBDocumentCollectionManager(pool);
        } catch (IOException e) {
            throw new OrientDBException("Error when getDocumentEntityManager", e);
        }
    }

    @Override
    public OrientDBDocumentCollectionManagerAsync getAsync(String database) throws UnsupportedOperationException, NullPointerException {
        try {
            OServerAdmin serverAdmin = new OServerAdmin(host)
                    .connect(user, password);

            if (!serverAdmin.existsDatabase(database, storageType)) {
                serverAdmin.createDatabase(database, DATABASE_TYPE, storageType);
            }
            OPartitionedDatabasePool pool = new OPartitionedDatabasePool("remote:" + host + '/' + database, user, password);
            return new OrientDBDocumentCollectionManagerAsync(pool);
        } catch (IOException e) {
            throw new OrientDBException("Error when getDocumentEntityManager", e);
        }
    }

    @Override
    public void close() {

    }
}
