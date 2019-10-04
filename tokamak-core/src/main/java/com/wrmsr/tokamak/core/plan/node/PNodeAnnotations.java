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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;

import javax.annotation.concurrent.Immutable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PNodeAnnotations
        extends AnnotationCollection<PNodeAnnotation, PNodeAnnotations>
{
    @Immutable
    public static final class Fields
            extends AnnotationCollectionMap<String, FieldAnnotation, Fields.Entry, Fields>
    {
        @Immutable
        public static final class Entry
                extends AnnotationCollectionMap.Entry<String, FieldAnnotation, PNodeAnnotations.Fields.Entry>
        {
            @JsonCreator
            public Entry(
                    @JsonProperty("key") String key,
                    @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
            {
                super(checkNotEmpty(key), annotations);
            }

            @JsonProperty("key")
            @Override
            public String getKey()
            {
                return super.getKey();
            }

            @JsonProperty("annotations")
            @Override
            public List<FieldAnnotation> getAnnotations()
            {
                return super.getAnnotations();
            }

            @Override
            public Class<FieldAnnotation> getAnnotationCls()
            {
                return FieldAnnotation.class;
            }

            @Override
            protected Entry rebuildWithAnnotations(Iterable<FieldAnnotation> annotations)
            {
                return new Entry(key, annotations);
            }
        }

        @JsonCreator
        public Fields(
                @JsonProperty("entries") Iterable<Entry> entries)
        {
            super(entries);
        }

        @JsonProperty("entries")
        @Override
        public Collection<Entry> getEntries()
        {
            return super.getEntries();
        }

        @Override
        protected Fields rebuildWithEntries(Iterable<Entry> entries)
        {
            return new Fields(entries);
        }

        @Override
        protected Entry newEntry(String key, Iterable<FieldAnnotation> annotations)
        {
            return new Entry(key, annotations);
        }
    }

    private final Fields fields;

    @JsonCreator
    public PNodeAnnotations(
            @JsonProperty("annotations") Iterable<PNodeAnnotation> annotations,
            @JsonProperty("fields") Fields fields)
    {
        super(annotations);

        this.fields = checkNotNull(fields);
    }

    @JsonProperty("annotations")
    @Override
    public List<PNodeAnnotation> getAnnotations()
    {
        return super.getAnnotations();
    }

    @JsonProperty("fields")
    public Fields getField()
    {
        return fields;
    }

    public PNodeAnnotations()
    {
        this(ImmutableList.of(), new Fields(ImmutableList.of()));
    }

    private static final PNodeAnnotations EMPTY = new PNodeAnnotations();

    public static PNodeAnnotations empty()
    {
        return EMPTY;
    }

    @Override
    public Class<PNodeAnnotation> getAnnotationCls()
    {
        return PNodeAnnotation.class;
    }

    @Override
    protected PNodeAnnotations rebuildWithAnnotations(Iterable<PNodeAnnotation> annotations)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations withFields(Fields fields)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations mapFields(Function<Fields, Fields> fn)
    {
        return new PNodeAnnotations(annotations, fn.apply(fields));
    }
}
