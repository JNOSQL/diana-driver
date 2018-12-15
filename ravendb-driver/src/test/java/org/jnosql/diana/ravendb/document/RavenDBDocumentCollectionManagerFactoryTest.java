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

package org.jnosql.diana.ravendb.document;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RavenDBDocumentCollectionManagerFactoryTest {

    private static RavenDBDocumentConfiguration configuration;

    @BeforeAll
    public static void setUp() throws IOException {
        configuration = new RavenDBDocumentConfiguration();
    }

    @Test
    public void shouldCreateEntityManager() {
        RavenDBDocumentCollectionManagerFactory ravenDBFactory = configuration.get();
        assertNotNull(ravenDBFactory.get("database"));
    }

    @Test
    public void shouldReturnNPEWhenSettingsIsNull() {
        assertThrows(NullPointerException.class, () -> configuration.get(null));
    }


}