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


import org.elasticsearch.index.query.QueryBuilder;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentEntity;

import java.util.List;

/**
 * The ES implementation of {@link DocumentCollectionManager}
 */
public interface ElasticsearchDocumentCollectionManager extends DocumentCollectionManager {

    /**
     * Find entities from {@link QueryBuilder}
     *
     * @param query the query
     * @param types the types
     * @return the objects from query
     * @throws NullPointerException when query is null
     */
     List<DocumentEntity> search(QueryBuilder query, String... types) throws NullPointerException;


}
