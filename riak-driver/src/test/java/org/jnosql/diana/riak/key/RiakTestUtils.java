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
package org.jnosql.diana.riak.key;


import com.basho.riak.client.core.RiakNode;
import org.jnosql.diana.api.key.BucketManagerFactory;

public final class RiakTestUtils {


    public static BucketManagerFactory get() {
        RiakConfiguration riakConfiguration = new RiakConfiguration();
        RiakNode node = new RiakNode.Builder()
                .withRemoteAddress("localhost").build();
        riakConfiguration.add(node);
        return riakConfiguration.get();
    }
}
