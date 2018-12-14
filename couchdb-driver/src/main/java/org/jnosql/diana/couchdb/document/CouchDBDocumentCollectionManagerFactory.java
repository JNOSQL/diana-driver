/*
 *
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
 *
 */
package org.jnosql.diana.couchdb.document;

import org.jnosql.diana.api.document.DocumentCollectionManagerAsyncFactory;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;

import java.util.Objects;

public class CouchDBDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory<CouchDBDocumentCollectionManager>,
        DocumentCollectionManagerAsyncFactory<CouchDBDocumentCollectionManagerAsync> {


    private final CouchDBHttpConfiguration configuration;

    CouchDBDocumentCollectionManagerFactory(CouchDBHttpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public CouchDBDocumentCollectionManager get(String database) {
        Objects.requireNonNull(database, "database is required");

        CouchDBHttpClient client = configuration.getClient(database);
        client.createDatabase();
        return new DefaultCouchDBDocumentCollectionManager(client);
    }

    @Override
    public CouchDBDocumentCollectionManagerAsync getAsync(String database) {
        Objects.requireNonNull(database, "database is required");
        CouchDBDocumentCollectionManager manager = get(database);
        return new DefaultCouchDBDocumentCollectionManagerAsync(manager);
    }

    @Override
    public void close() {
    }
}
