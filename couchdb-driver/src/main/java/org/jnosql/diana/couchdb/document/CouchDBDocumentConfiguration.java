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

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.SettingsBuilder;
import org.jnosql.diana.api.document.UnaryDocumentConfiguration;
import org.jnosql.diana.driver.ConfigurationReader;

import java.util.Map;
import java.util.Objects;

/**
 * The CouchDB implementation of {@link org.jnosql.diana.api.document.DocumentConfiguration} that returns
 * {@link CouchDBDocumentCollectionManagerFactory}, settings:
 * <p>couchdb.port: </p>
 * <p>couchdb.max.connections: </p>
 * <p>couchdb.connection.timeout: </p>
 * <p>couchdb.socket.timeout: </p>
 * <p>couchdb.max.object.size.bytes: </p>
 * <p>couchdb.max.cache.entries: </p>
 * <p>couchdb.host: </p>
 * <p>couchdb.username: </p>
 * <p>couchdb.password: </p>
 * <p>couchdb.enable.ssl: </p>
 * <p>couchdb.compression: </p>
 */
public class CouchDBDocumentConfiguration implements UnaryDocumentConfiguration<CouchDBDocumentCollectionManagerFactory> {

    public static final String PORT = "couchdb.port";
    public static final String MAX_CONNECTIONS = "couchdb.max.connections";
    public static final String CONNECTION_TIMEOUT = "couchdb.connection.timeout";
    public static final String SOCKET_TIMEOUT = "couchdb.socket.timeout";
    public static final String MAX_OBJECT_SIZE_BYTES = "couchdb.max.object.size.bytes";
    public static final String MAX_CACHE_ENTRIES = "couchdb.max.cache.entries";
    public static final String HOST = "couchdb.host";
    public static final String USERNAME = "couchdb.username";
    public static final String PASSWORD = "couchdb.password";

    public static final String ENABLE_SSL = "couchdb.enable.ssl";
    public static final String COMPRESSION = "couchdb.compression";

    private static final String FILE_CONFIGURATION = "diana-couchdb.properties";

    @Override
    public CouchDBDocumentCollectionManagerFactory get() {
        Map<String, String> configuration = ConfigurationReader.from(FILE_CONFIGURATION);
        SettingsBuilder builder = Settings.builder();
        configuration.entrySet().forEach(e -> builder.put(e.getKey(), e.getValue()));
        return get(builder.build());
    }

    @Override
    public CouchDBDocumentCollectionManagerFactory get(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");
        CouchDBHttpConfigurationBuilder configuration = new CouchDBHttpConfigurationBuilder();
        settings.computeIfPresent(PORT, (k, v) -> configuration.withPort(Integer.valueOf(v.toString())));
        settings.computeIfPresent(MAX_CONNECTIONS, (k, v) -> configuration.withMaxConnections(Integer.valueOf(v.toString())));
        settings.computeIfPresent(CONNECTION_TIMEOUT, (k, v) -> configuration.withConnectionTimeout(Integer.valueOf(v.toString())));
        settings.computeIfPresent(SOCKET_TIMEOUT, (k, v) -> configuration.withSocketTimeout(Integer.valueOf(v.toString())));
        settings.computeIfPresent(MAX_OBJECT_SIZE_BYTES, (k, v) -> configuration.withMaxObjectSizeBytes(Integer.valueOf(v.toString())));
        settings.computeIfPresent(MAX_CACHE_ENTRIES, (k, v) -> configuration.withMaxCacheEntries(Integer.valueOf(v.toString())));
        settings.computeIfPresent(HOST, (k, v) -> configuration.withHost(v.toString()));
        settings.computeIfPresent(USERNAME, (k, v) -> configuration.withUsername(v.toString()));
        settings.computeIfPresent(PASSWORD, (k, v) -> configuration.withPassword(v.toString()));
        settings.computeIfPresent(ENABLE_SSL, (k, v) -> configuration.withEnableSSL(Boolean.valueOf(v.toString())));
        settings.computeIfPresent(COMPRESSION, (k, v) -> configuration.withCompression(Boolean.valueOf(v.toString())));
        return new CouchDBDocumentCollectionManagerFactory(configuration.build());
    }

    @Override
    public CouchDBDocumentCollectionManagerFactory getAsync() {
        return get();
    }

    @Override
    public CouchDBDocumentCollectionManagerFactory getAsync(Settings settings) {
        return get(settings);
    }
}