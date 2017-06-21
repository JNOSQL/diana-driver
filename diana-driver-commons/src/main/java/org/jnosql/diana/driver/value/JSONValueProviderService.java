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
package org.jnosql.diana.driver.value;


import java.util.Objects;
import java.util.ServiceLoader;

public final class JSONValueProviderService {

    private static final JSONValueProvider PROVIDER;

    private JSONValueProviderService() {
    }

    static {
        JSONValueProvider aux = null;
        for (JSONValueProvider jsonValueProvider : ServiceLoader.load(JSONValueProvider.class)) {
            if (Objects.nonNull(jsonValueProvider)) {
                aux = jsonValueProvider;
            }
        }

        if (Objects.isNull(aux)) {
            PROVIDER = new JSONGSONValueProvider();
        } else {
            PROVIDER = aux;
        }

    }


    public static JSONValueProvider getProvider() {
        return new JSONGSONValueProvider();
    }
}
