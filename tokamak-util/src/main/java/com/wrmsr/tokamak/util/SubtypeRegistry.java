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
package com.wrmsr.tokamak.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public class SubtypeRegistry<T, E extends SubtypeRegistry.Entry<T>>
{
    public static class Entry<T>
    {
        private final String name;
        private final Class<? extends T> cls;

        public Entry(String name, Class<? extends T> cls)
        {
            this.name = checkNotEmpty(name);
            this.cls = checkNotNull(cls);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Entry<?> entry = (Entry<?>) o;
            return Objects.equals(name, entry.name) &&
                    Objects.equals(cls, entry.cls);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, cls);
        }

        @Override
        public String toString()
        {
            return "Entry{" +
                    "name='" + name + '\'' +
                    ", cls=" + cls +
                    '}';
        }

        public String getName()
        {
            return name;
        }

        public Class<? extends T> getCls()
        {
            return cls;
        }
    }

    public static <T> Entry<T> entry(String name, Class<T> cls)
    {
        return new Entry<>(name, cls);
    }

    private static final class State<T, E extends Entry<T>>
    {
        private final List<E> entries;

        private final Map<String, E> entriesByName;
        private final Map<Class<? extends T>, E> entriesByCls;

        private final Map<String, Class<? extends T>> clsByName;
        private final Map<Class<? extends T>, String> nameByCls;

        public State(Iterable<E> entries)
        {
            this.entries = ImmutableList.copyOf(entries);

            ImmutableMap.Builder<String, E> entriesByName = ImmutableMap.builder();
            ImmutableMap.Builder<Class<? extends T>, E> entriesByCls = ImmutableMap.builder();

            ImmutableMap.Builder<String, Class<? extends T>> clsByName = ImmutableMap.builder();
            ImmutableMap.Builder<Class<? extends T>, String> nameByCls = ImmutableMap.builder();

            for (E entry : this.entries) {
                entriesByName.put(entry.getName(), entry);
                entriesByCls.put(entry.getCls(), entry);

                clsByName.put(entry.getName(), entry.getCls());
                nameByCls.put(entry.getCls(), entry.getName());
            }

            this.entriesByName = entriesByName.build();
            this.entriesByCls = entriesByCls.build();

            this.clsByName = clsByName.build();
            this.nameByCls = nameByCls.build();
        }

        @Override
        public String toString()
        {
            return "State{" +
                    "entries=" + entries +
                    '}';
        }
    }

    private final Class<T> cls;
    private final Class<E> entryCls;

    private final Object lock = new Object();
    private volatile State<T, E> state = new State<>(ImmutableList.of());

    public SubtypeRegistry(Class<T> cls, Class<E> entryCls)
    {
        this.cls = checkNotNull(cls);
        this.entryCls = checkNotNull(entryCls);
    }

    public SubtypeRegistry(Class<T> cls, Class<E> entryCls, Iterable<E> entries)
    {
        this(cls, entryCls);
        put(entries);
    }

    public Class<T> getCls()
    {
        return cls;
    }

    public Class<E> getEntryCls()
    {
        return entryCls;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> SubtypeRegistry<T, SubtypeRegistry.Entry<T>> simple(Class<T> cls)
    {
        return (SubtypeRegistry<T, SubtypeRegistry.Entry<T>>) new SubtypeRegistry(cls, SubtypeRegistry.Entry.class);
    }

    public static <T> SubtypeRegistry<T, SubtypeRegistry.Entry<T>> simple(Class<T> cls, Map<String, Class<? extends T>> map)
    {
        SubtypeRegistry<T, SubtypeRegistry.Entry<T>> registry = simple(cls);
        registry.put(map.entrySet().stream().map(e -> new SubtypeRegistry.Entry<T>(e.getKey(), e.getValue())).iterator());
        return registry;
    }

    @Override
    public String toString()
    {
        return "SubtypeRegistry{" +
                "cls=" + cls +
                ", entries=" + state.entries +
                '}';
    }

    public Map<String, Class<? extends T>> getNameClsMap()
    {
        return state.clsByName;
    }

    public Map<Class<? extends T>, String> getClsNameMap()
    {
        return state.nameByCls;
    }

    public Optional<E> getEntry(String name)
    {
        return Optional.ofNullable(state.entriesByName.get(name));
    }

    public Optional<Class<? extends T>> get(String name)
    {
        return getEntry(name).map(Entry::getCls);
    }

    public Optional<E> getEntry(Class<? extends T> cls)
    {
        return Optional.ofNullable(state.entriesByCls.get(cls));
    }

    public Optional<String> get(Class<? extends T> cls)
    {
        return getEntry(cls).map(Entry::getName);
    }

    public SubtypeRegistry<T, E> put(Iterator<E> entries)
    {
        synchronized (lock) {
            List<E> entryList = ImmutableList.copyOf(entries);
            entryList.forEach(e -> {
                checkArgument(entryCls.isInstance(e));
                checkArgument(cls.isAssignableFrom(e.getCls()));
            });
            state = new State<>(ImmutableList.<E>builder()
                    .addAll(state.entries)
                    .addAll(entryList)
                    .build());
        }

        return this;
    }

    public SubtypeRegistry<T, E> put(Iterable<E> entries)
    {
        return put(entries.iterator());
    }

    public SubtypeRegistry<T, E> put(E entry)
    {
        return put(Iterators.singletonIterator(entry));
    }

    public ObjectMapper register(ObjectMapper objectMapper)
    {
        for (E entry : state.entries) {
            objectMapper.registerSubtypes(new NamedType(entry.getCls(), entry.getName()));
        }
        return objectMapper;
    }

    public static <T> void checkRegistered(
            ObjectMapper objectMapper,
            Class<T> cls,
            Iterable<Class<? extends T>> subclsList)
    {
        Collection<NamedType> subtypes = objectMapper.getSubtypeResolver().collectAndResolveSubtypesByTypeId(
                objectMapper.getSerializationConfig(),
                objectMapper.getSerializationConfig().introspect(objectMapper.getTypeFactory().constructType(cls)).getClassInfo());
        subtypes.forEach(st -> checkNotNull(st.getName()));
        Set<Class> subtypeSet = subtypes.stream().map(NamedType::getType).collect(toImmutableSet());
        for (Class<? extends T> subcls : subclsList) {
            if (!subtypeSet.contains(subcls)) {
                throw new IllegalStateException("Subtype type not registered: " + subcls);
            }
        }
    }

    public void checkRegistered(ObjectMapper objectMapper)
    {
        checkRegistered(objectMapper, cls, state.nameByCls.keySet());
    }
}
