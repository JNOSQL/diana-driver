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
package org.jnosql.diana.elasticsearch.document;


import org.elasticsearch.client.Client;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsyncFactory;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.nio.file.Files.readAllBytes;


/**
 * The elasticsearch implementation to {@link DocumentCollectionManagerFactory} that returns:
 * {@link ElasticsearchDocumentCollectionManager} and {@link ElasticsearchDocumentCollectionManagerAsync}.
 * If the database does not exist, it tries to read a json mapping from the database name.
 * Eg: {@link ElasticsearchDocumentCollectionManagerFactory#get(String)} with database, if does not exist it tries to
 * read a "/database.json" file. The file must have the mapping to elasticsearch.
 */
public class ElasticsearchDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory<ElasticsearchDocumentCollectionManager>,
        DocumentCollectionManagerAsyncFactory<ElasticsearchDocumentCollectionManagerAsync> {


    private final Client client;

    ElasticsearchDocumentCollectionManagerFactory(Client client) {
        this.client = client;
    }

    @Override
    public ElasticsearchDocumentCollectionManagerAsync getAsync(String database) throws UnsupportedOperationException, NullPointerException {
        initDatabase(database);
        return new ElasticsearchDocumentCollectionManagerAsync(client, database);
    }


    @Override
    public ElasticsearchDocumentCollectionManager get(String database) throws UnsupportedOperationException, NullPointerException {
        Objects.requireNonNull(database, "database is required");

        initDatabase(database);
        return new ElasticsearchDocumentCollectionManager(client, database);
    }

    private byte[] getBytes(URL url) {
        try {
            return readAllBytes(Paths.get(url.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new ElasticsearchException("An error when read the database mapping", e);
        }
    }

    private void initDatabase(String database) {
        boolean exists = isExists(database);
        if (!exists) {
            URL url = ElasticsearchDocumentCollectionManagerFactory.class.getResource('/' + database + ".json");
            if (Objects.nonNull(url)) {
                byte[] bytes = getBytes(url);
                client.admin().indices().prepareCreate(database).setSource(bytes).get();
            }
        }
    }

    private boolean isExists(String database) {
        try {
            return client.admin().indices().prepareExists(database).execute().get().isExists();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElasticsearchException("And error on admin access to verify if the database exists", e);
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
