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

package org.jnosql.diana.driver.value;


import org.jnosql.diana.api.Value;

import java.util.Objects;

/**
 * The implementation that uses {@link JSONGSONValue}
 */
public class JSONGSONValueProvider implements JSONValueProvider {

    @Override
    public Value of(String json) throws NullPointerException, UnsupportedOperationException {
        Objects.requireNonNull(json, "Json is required");
        return JSONGSONValue.of(json);
    }

    @Override
    public Value of(byte[] json) throws NullPointerException, UnsupportedOperationException {
        Objects.requireNonNull(json, "Json is required");
        return JSONGSONValue.of(String.valueOf(json));
    }

    @Override
    public String toJson(Object object) throws NullPointerException, UnsupportedOperationException {
        return JSONGSONValue.GSON.toJson(Objects.requireNonNull(object, "object is required"));
    }

    @Override
    public byte[] toJsonArray(Object object) throws NullPointerException, UnsupportedOperationException {
        return toJson(object).getBytes();
    }
}
