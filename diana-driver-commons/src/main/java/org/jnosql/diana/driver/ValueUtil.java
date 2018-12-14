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
package org.jnosql.diana.driver;


import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.ValueWriter;
import org.jnosql.diana.api.writer.ValueWriterDecorator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Utilitarian class to {@link Value}
 */
public final class ValueUtil {

    private static final ValueWriter VALUE_WRITER = ValueWriterDecorator.getInstance();
    private static final Function CONVERT = o -> {
        if (o instanceof Value) {
            return convert(Value.class.cast(o));
        }
        return getObject(o);
    };

    private ValueUtil() {
    }

    /**
     * converter a {@link Value} to Object
     *
     * @param value the value
     * @return a object converted
     */
    public static Object convert(Value value) {
        Objects.requireNonNull(value, "value is required");
        Object val = value.get();
        if(val instanceof Iterable) {
            return getObjects(val);
        }
        return getObject(val);
    }


    /**
     * Converts the {@link Value} to {@link List}
     *
     * @param value the value
     * @return a list object
     */
    public static List<Object> convertToList(Value value) {
        Objects.requireNonNull(value, "value is required");
        Object val = value.get();
        if(val instanceof Iterable) {
            return getObjects(val);

        }
        return Collections.singletonList(getObject(val));
    }

    private static List<Object> getObjects(Object val) {
        return (List<Object>) StreamSupport.stream(Iterable.class.cast(val).spliterator(), false)
                .map(CONVERT).collect(toList());
    }

    private static Object getObject(Object val) {
        if (VALUE_WRITER.isCompatible(val.getClass())) {
            return VALUE_WRITER.write(val);
        }
        return val;
    }
}
