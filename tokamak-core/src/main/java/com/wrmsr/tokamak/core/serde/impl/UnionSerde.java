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
package com.wrmsr.tokamak.core.serde.impl;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Width;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@SuppressWarnings({"rawtypes"})
@Immutable
public final class UnionSerde
        implements Serde<Object>
{
    public static final class Entry<T>
    {
        private final Class<T> cls;
        private final byte tag;
        private final Serde<T> child;

        public Entry(Class<T> cls, byte tag, Serde<T> child)
        {
            this.cls = checkNotNull(cls);
            this.tag = tag;
            this.child = checkNotNull(child);
        }

        @Override
        public String toString()
        {
            return "Entry{" +
                    "cls=" + cls +
                    ", tag=" + tag +
                    ", child=" + child +
                    '}';
        }

        public Class<T> getCls()
        {
            return cls;
        }

        public byte getTag()
        {
            return tag;
        }

        public Serde<T> getChild()
        {
            return child;
        }
    }

    @FunctionalInterface
    public interface UnknownClsEncoder
    {
        void encode(Object value, Output output);
    }

    @FunctionalInterface
    public interface UnknownTagDecoder
    {
        Object decode(byte tag, Input input);
    }

    private final List<Entry> entries;
    private final UnknownClsEncoder unknownClsEncoder;
    private final UnknownTagDecoder unknownTagDecoder;

    private final Map<Class, Entry> entriesByCls;
    private final Map<Byte, Entry> entriesByTag;

    public UnionSerde(List<Entry> entries, UnknownClsEncoder unknownClsEncoder, UnknownTagDecoder unknownTagDecoder)
    {
        this.entries = ImmutableList.copyOf(entries);
        this.unknownClsEncoder = checkNotNull(unknownClsEncoder);
        this.unknownTagDecoder = checkNotNull(unknownTagDecoder);

        entriesByCls = this.entries.stream().collect(toImmutableMap(Entry::getCls, identity()));
        entriesByTag = this.entries.stream().collect(toImmutableMap(Entry::getTag, identity()));
    }

    public UnionSerde(List<Entry> entries)
    {
        this(
                entries,
                (v, o) -> {throw new IllegalArgumentException("Unsupported union cls: " + v);},
                (t, i) -> {throw new IllegalArgumentException("Unsupported union tag: " + t);});
    }

    public List<Entry> getEntries()
    {
        return entries;
    }

    public UnknownClsEncoder getUnknownClsEncoder()
    {
        return unknownClsEncoder;
    }

    public UnknownTagDecoder getUnknownTagDecoder()
    {
        return unknownTagDecoder;
    }

    public Map<Class, Entry> getEntriesByCls()
    {
        return entriesByCls;
    }

    public Map<Byte, Entry> getEntriesByTag()
    {
        return entriesByTag;
    }

    private final SupplierLazyValue<Width> width = new SupplierLazyValue<>();

    @Override
    public Width getWidth()
    {
        return width.get(() ->
                Width.reduce(
                        0,
                        Integer::sum,
                        immutableMapItems(entries, e -> e.getChild().getWidth())
                ).map(w -> w + 1));
    }

    @Override
    public boolean isNullable()
    {
        return entries.stream().anyMatch(e -> e.getChild().isNullable());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void write(Object value, Output output)
    {
        Entry entry = entriesByCls.get(value.getClass());
        if (entry == null) {
            unknownClsEncoder.encode(value, output);
        }
        else {
            output.put(entry.tag);
            entry.child.write(value, output);
        }
    }

    @Override
    public Object read(Input input)
    {
        byte tag = input.get();
        Entry entry = entriesByTag.get(tag);
        if (entry == null) {
            return unknownTagDecoder.decode(tag, input);
        }
        else {
            return entry.child.read(input);
        }
    }
}
