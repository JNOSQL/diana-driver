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

import org.jnosql.diana.api.Sort;
import org.jnosql.diana.api.document.DocumentCondition;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.jnosql.diana.couchdb.document.CouchDBConstant.ID;
import static org.jnosql.diana.couchdb.document.CouchDBConstant.REV;

final class DeleteQuery implements DocumentQuery {

    private static final List<String> DOCUMENTS = asList(ID, REV);
    private final DocumentDeleteQuery query;

    DeleteQuery(DocumentDeleteQuery query) {
        this.query = query;
    }

    @Override
    public long getLimit() {
        return 10;
    }

    @Override
    public long getSkip() {
        return 0;
    }

    @Override
    public String getDocumentCollection() {
        return query.getDocumentCollection();
    }

    @Override
    public Optional<DocumentCondition> getCondition() {
        return query.getCondition();
    }

    @Override
    public List<Sort> getSorts() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getDocuments() {
        return DOCUMENTS;
    }
}
