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
package com.wrmsr.tokamak.core.layout.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.collect.StreamableIterable;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Map<String, Type> typesByName;

    @JsonCreator
    public FieldCollection(
            @JsonProperty("fields") List<Field> fields)
    {
        this.fields = ImmutableList.copyOf(fields);
        checkNotEmpty(this.fields);

        fieldsByName = this.fields.stream().collect(toImmutableMap(Field::getName, identity()));
        typesByName = this.fields.stream().collect(toImmutableMap(Field::getName, Field::getType));
    }

    public boolean contains(String name)
    {
        return fieldsByName.containsKey(name);
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

    public Set<String> getNames()
    {
        return fieldsByName.keySet();
    }

    public Map<String, Field> getFieldsByName()
    {
        return fieldsByName;
    }

    public Map<String, Type> getTypesByName()
    {
        return typesByName;
    }

    public Field get(String name)
    {
        return checkNotNull(fieldsByName.get(name));
    }

    public Type getType(String name)
    {
        return checkNotNull(getFieldsByName().get(name)).getType();
    }

    private final SupplierLazyValue<Map<Class<? extends FieldAnnotation>, List<Field>>> fieldListsByAnnotationCls = new SupplierLazyValue<>();

    public Map<Class<? extends FieldAnnotation>, List<Field>> getFieldListsByAnnotationCls()
    {
        return fieldListsByAnnotationCls.get(() -> {
            Map<Class<? extends FieldAnnotation>, List<Field>> listsByCls = new LinkedHashMap<>();
            fields.forEach(f -> f.getAnnotationsByCls().keySet().forEach(ac -> listsByCls.computeIfAbsent(ac, ac_ -> new ArrayList<>()).add(f)));
            return listsByCls.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())));
        });
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

    @SafeVarargs
    public final FieldCollection withoutAnnotation(Class<? extends FieldAnnotation>... annotationCls)
    {
        return builder().addAll(fields.stream().map(f -> f.withoutAnnotation(annotationCls))).build();
    }
}