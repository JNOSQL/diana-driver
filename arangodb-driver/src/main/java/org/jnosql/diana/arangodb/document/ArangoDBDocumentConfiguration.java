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

package org.jnosql.diana.arangodb.document;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.document.UnaryDocumentConfiguration;
import org.jnosql.diana.arangodb.ArangoDBConfiguration;

import static java.util.Objects.requireNonNull;

/**
 * The implementation of {@link UnaryDocumentConfiguration} that returns {@link ArangoDBDocumentCollectionManagerFactory}.
 * It tries to read the configuration properties from arangodb.properties file.
 *
 * @see ArangoDBConfiguration
 * The Properties:
 * <p>arangodb-host: the host</p>
 * <p>arangodb-user: the user</p>
 * <p>arangodb-password: the password</p>
 * <p>arangodb-port: the port</p>
 * <p>arangodb-timeout: the timeout</p>
 * <p>arangodb-chuckSize: the chuckSize</p>
 * <p>arangodb-userSsl: the userSsl</p>
 * <p>arangodb-loadBalancingStrategy: the define loadBalancingStrategy</p>
 * <p>arangodb.hosts:  the hosts</p>
 * <p>arangodb.protocol: the protocol</p>
 * <p>arangodb.chunksize: the chunksize</p>
 * <p>arangodb.connections.max: the max connection</p>
 * <p>arangodb.acquireHostList: the max connection</p>
 *
 */
public class ArangoDBDocumentConfiguration extends ArangoDBConfiguration
        implements UnaryDocumentConfiguration<ArangoDBDocumentCollectionManagerFactory> {


    @Override
    public ArangoDBDocumentCollectionManagerFactory get() throws UnsupportedOperationException {
        return new ArangoDBDocumentCollectionManagerFactory(builder.build(), builderAsync.build());
    }

    @Override
    public ArangoDBDocumentCollectionManagerFactory get(Settings settings) throws NullPointerException {
        requireNonNull(settings, "settings is required");

        ArangoDB arangoDB = getArangoDB(settings);
        ArangoDBAsync arangoDBAsync = getArangoDBAsync(settings);
        return new ArangoDBDocumentCollectionManagerFactory(arangoDB, arangoDBAsync);
    }

    @Override
    public ArangoDBDocumentCollectionManagerFactory getAsync() throws UnsupportedOperationException {
        return new ArangoDBDocumentCollectionManagerFactory(builder.build(), builderAsync.build());
    }

    @Override
    public ArangoDBDocumentCollectionManagerFactory getAsync(Settings settings) throws NullPointerException {
        return get(settings);
    }
}
