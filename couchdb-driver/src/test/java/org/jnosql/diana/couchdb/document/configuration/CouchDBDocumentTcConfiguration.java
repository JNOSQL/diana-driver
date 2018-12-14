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
package org.jnosql.diana.couchdb.document.configuration;

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.SettingsBuilder;
import org.jnosql.diana.couchdb.document.CouchDBDocumentCollectionManagerFactory;
import org.jnosql.diana.couchdb.document.CouchDBDocumentConfiguration;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Supplier;

public enum CouchDBDocumentTcConfiguration implements Supplier<CouchDBDocumentCollectionManagerFactory> {

    INSTANCE;


    private GenericContainer couchDB = new CouchDBContainer();

    {
        couchDB.start();
    }

    @Override
    public CouchDBDocumentCollectionManagerFactory get() {
        CouchDBDocumentConfiguration configuration = new CouchDBDocumentConfiguration();
        SettingsBuilder builder = Settings.builder();
        builder.put(CouchDBDocumentConfiguration.PORT, couchDB.getFirstMappedPort());
        return configuration.get(builder.build());
    }

}
