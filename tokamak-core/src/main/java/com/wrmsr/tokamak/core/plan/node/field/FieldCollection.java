/*
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
 */
package com.wrmsr.tokamak.core.plan.node.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

@Immutable
public final class FieldCollection
        implements StreamableIterable<Field>
{
    private final List<Field> fields;

    private final Map<String, Field> fieldsByName;

    @JsonCreator
    public FieldCollection(
            @JsonProperty("fields") List<Field> fields)
    {
        this.fields = ImmutableList.copyOf(fields);
        checkNotEmpty(this.fields);

        fieldsByName = this.fields.stream().collect(toImmutableMap(Field::getName, identity()));
    }

    public int size()
    {
        return fields.size();
    }

    @Override
    public Iterator<Field> iterator()
    {
        return fields.iterator();
    }

    @JsonProperty
    public List<Field> getFields()
    {
        return fields;
    }

    public Map<String, Field> getFieldsByName()
    {
        return fieldsByName;
    }

    public Field get(String name)
    {
        return checkNotNull(fieldsByName.get(name));
    }

    public static final class Builder
    {
        private final ImmutableList.Builder<Field> fields = ImmutableList.builder();

        private Builder()
        {
        }

        public Builder add(Field field)
        {
            this.fields.add(field);
            return this;
        }

        public Builder add(Field... fields)
        {
            this.fields.add(fields);
            return this;
        }

        public Builder addAll(Iterable<Field> fields)
        {
            this.fields.addAll(fields);
            return this;
        }

        public Builder addAll(Iterator<Field> fields)
        {
            this.fields.addAll(fields);
            return this;
        }

        public Builder addAll(Stream<Field> fields)
        {
            fields.forEachOrdered(this.fields::add);
            return this;
        }

        public Builder add(String name, Type type)
        {
            fields.add(new Field(name, type));
            return this;
        }

        public Builder addAll(Map<String, Type> typesByName)
        {
            checkOrdered(typesByName).forEach(this::add);
            return this;
        }

        public FieldCollection build()
        {
            return new FieldCollection(fields.build());
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static FieldCollection of(List<Field> fields)
    {
        return builder().addAll(fields).build();
    }

    public static FieldCollection of(Map<String, Type> typesByName)
    {
        return builder().addAll(typesByName).build();
    }
}
