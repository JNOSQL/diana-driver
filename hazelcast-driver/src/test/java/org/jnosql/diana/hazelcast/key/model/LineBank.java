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

package org.jnosql.diana.hazelcast.key.model;


import java.io.Serializable;
import java.util.Objects;

public class LineBank implements Serializable {


    private final Person person;

    public Person getPerson() {
        return person;
    }

    public LineBank(String name, Integer age) {
        this.person = new Person(name, age);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineBank lineBank = (LineBank) o;
        return Objects.equals(person, lineBank.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LineBank{");
        sb.append("person=").append(person);
        sb.append('}');
        return sb.toString();
    }
}