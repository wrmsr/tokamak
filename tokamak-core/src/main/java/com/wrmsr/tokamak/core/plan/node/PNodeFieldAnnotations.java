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
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PNodeFieldAnnotations
        extends AnnotationCollectionMap<String, FieldAnnotation, PNodeFieldAnnotations.Entry, PNodeFieldAnnotations>
{
    @Immutable
    public static final class Entry
            extends AnnotationCollectionMap.Entry<String, FieldAnnotation, Entry>
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
        public List<FieldAnnotation> get()
        {
            return super.get();
        }

        @Override
        public Class<FieldAnnotation> getBaseCls()
        {
            return FieldAnnotation.class;
        }

        @Override
        protected Entry rebuildWith(Iterable<FieldAnnotation> annotations)
        {
            return new Entry(key, annotations);
        }
    }

    @JsonCreator
    public PNodeFieldAnnotations(
            @JsonProperty("entries") Iterable<Entry> entries)
    {
        super(entries);
    }

    @JsonProperty("entries")
    @Override
    public List<Entry> getEntries()
    {
        return super.getEntries();
    }

    @Override
    protected PNodeFieldAnnotations rebuildWithEntries(Iterable<Entry> entries)
    {
        return new PNodeFieldAnnotations(entries);
    }

    @Override
    protected Entry newEntry(String key, Iterable<FieldAnnotation> annotations)
    {
        return new Entry(key, annotations);
    }

    private final SupplierLazyValue<Map<Class<? extends FieldAnnotation>, Set<String>>> keySetsByAnnotationCls = new SupplierLazyValue<>();

    public Map<Class<? extends FieldAnnotation>, Set<String>> getKeySetsByAnnotationCls()
    {
        return keySetsByAnnotationCls.get(() ->
                immutableMapValues(getEntryListsByAnnotationCls(), l -> l.stream().map(Entry::getKey).collect(toImmutableSet())));
    }
}
