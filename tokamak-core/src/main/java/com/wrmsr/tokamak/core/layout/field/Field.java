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
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.util.Pair;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class Field
        extends AnnotationCollection<FieldAnnotation, Field>
{
    private final String name;
    private final Type type;

    private final Pair<String, Type> nameTypePair;

    @JsonCreator
    public Field(
            @JsonProperty("name") String name,
            @JsonProperty("type") Type type,
            @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
    {
        super(annotations);

        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);

        nameTypePair = Pair.immutable(name, type);
    }

    public Field(String name, Type type)
    {
        this(name, type, ImmutableList.of());
    }

    @Override
    public Class<FieldAnnotation> getAnnotationCls()
    {
        return FieldAnnotation.class;
    }

    @Override
    protected Field rebuildWithAnnotations(Iterable<FieldAnnotation> annotations)
    {
        return new Field(name, type, annotations);
    }

    @Override
    public String toString()
    {
        return "Field{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("type")
    public Type getType()
    {
        return type;
    }

    public Pair<String, Type> getNameTypePair()
    {
        return nameTypePair;
    }

    @JsonProperty("annotations")
    public List<FieldAnnotation> getAnnotations()
    {
        return annotations;
    }
}
