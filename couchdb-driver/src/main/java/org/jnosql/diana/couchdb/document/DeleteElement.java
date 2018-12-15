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
package org.jnosql.diana.couchdb.document;

import java.util.Map;
import java.util.Objects;

import static org.jnosql.diana.couchdb.document.CouchDBConstant.ID;
import static org.jnosql.diana.couchdb.document.CouchDBConstant.REV;

final class DeleteElement {

    private final String id;

    private final String rev;

    DeleteElement(Map<String, Object> json) {
        this.id = json.get(ID).toString();
        this.rev = json.get(REV).toString();
    }

    public String getId() {
        return id;
    }

    public String getRev() {
        return rev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeleteElement that = (DeleteElement) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(rev, that.rev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rev);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteElement{");
        sb.append("id='").append(id).append('\'');
        sb.append(", rev='").append(rev).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
