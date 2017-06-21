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


import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.ValueWriter;
import org.jnosql.diana.api.writer.ValueWriterDecorator;

public final class ValueUtil {

    private static final ValueWriter VALUE_WRITER = ValueWriterDecorator.getInstance();

    private ValueUtil() {
    }

    public static Object convert(Value value) {
        Object val = value.get();
        if (VALUE_WRITER.isCompatible(val.getClass())) {
            return VALUE_WRITER.write(val);
        }
        return val;
    }
}
