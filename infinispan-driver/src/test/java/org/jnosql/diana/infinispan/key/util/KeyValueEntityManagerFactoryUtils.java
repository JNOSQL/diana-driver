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
 *   The Infinispan Team
 */
package org.jnosql.diana.infinispan.key.util;

import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.api.key.KeyValueConfiguration;
import org.jnosql.diana.infinispan.key.InfinispanKeyValueConfiguration;


public class KeyValueEntityManagerFactoryUtils {

    public static BucketManagerFactory get() {
        KeyValueConfiguration configuration = new InfinispanKeyValueConfiguration();
        BucketManagerFactory managerFactory = configuration.get();
        return managerFactory;
    }
}
