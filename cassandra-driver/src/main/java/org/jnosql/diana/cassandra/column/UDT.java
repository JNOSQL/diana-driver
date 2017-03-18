/*
 * Copyright 2017 Otavio Santana and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.jnosql.diana.cassandra.column;


import org.jnosql.diana.api.column.Column;

import java.util.List;

/**
 * A Cassandra user data type, this interface does not support both Value alias method:
 * get(class) and get(TypeSupplier);
 */
public interface UDT extends Column {

    /**
     * The UDT name
     *
     * @return the UDT name
     */
    String getUserType();

    /**
     * The columns at this UDT
     *
     * @return the fields at UDT
     */
    List<Column> getColumns();

    /**
     * Returns a UDT builder
     *
     * @return the {@link UDTBuilder} instance
     */
    static UDTBuilder builder() {
        return new UDTBuilder();
    }
}
