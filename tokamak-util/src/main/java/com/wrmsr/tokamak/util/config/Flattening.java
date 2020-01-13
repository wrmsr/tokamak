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
package com.wrmsr.tokamak.util.config;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.arrayWithReplaced;
import static com.wrmsr.tokamak.util.MoreCollections.concatArrays;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Flattening
{
    public static final String DEFAULT_DELIMITER = ".";
    public static final String DEFAULT_INDEX_OPEN = "(";
    public static final String DEFAULT_INDEX_CLOSE = ")";

    private final String delimiter;
    private final String indexOpen;
    private final String indexClose;

    public Flattening(String delimiter, String indexOpen, String indexClose)
    {
        this.delimiter = checkNotEmpty(delimiter);
        this.indexOpen = checkNotEmpty(indexOpen);
        this.indexClose = checkNotEmpty(indexClose);
    }

    public Flattening()
    {
        this(DEFAULT_DELIMITER, DEFAULT_INDEX_OPEN, DEFAULT_INDEX_CLOSE);
    }

    private void flatten(ImmutableMap.Builder<String, Object> builder, String[] prefix, Object value)
    {
        if (value instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) value;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                flatten(builder, concatArrays(prefix, new String[] {e.getKey()}), e.getValue());
            }
        }
        else if (value instanceof List) {
            List<Object> l = (List<Object>) value;
            for (int i = 0; i < l.size(); ++i) {
                flatten(
                        builder,
                        arrayWithReplaced(prefix, prefix.length - 1, prefix[prefix.length - 1] + indexOpen + i + indexClose),
                        l.get(i));
            }
        }
        else {
            builder.put(Joiner.on(delimiter).join(prefix), value);
        }
    }

    public Map<String, Object> flatten(Map<String, Object> unflattened)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        flatten(builder, new String[] {}, unflattened);
        return builder.build();
    }

    private interface UnflattenNode<K>
    {
        Object get(K key);

        void put(K key, Object value);

        default <V> V setDefault(K key, Supplier<V> supplier)
        {
            V ret = (V) get(key);
            if (ret == null) {
                ret = supplier.get();
                put(key, ret);
            }
            return ret;
        }

        Object build();

        static Object build(Object value)
        {
            checkNotNull(value);
            return value instanceof UnflattenNode ? ((UnflattenNode) value).build() : value;
        }
    }

    private static final class UnflattenMap
            implements UnflattenNode<String>
    {
        private final Map<String, Object> map = new LinkedHashMap<>();

        @Override
        public Object get(String key)
        {
            return map.get(key);
        }

        @Override
        public void put(String key, Object value)
        {
            checkState(!map.containsKey(key));
            map.put(key, value);
        }

        @Override
        public Map<String, Object> build()
        {
            return map.entrySet().stream()
                    .collect(toImmutableMap(
                            Map.Entry::getKey,
                            e -> UnflattenNode.build(e.getValue())));
        }
    }

    private static final class UnflattenList
            implements UnflattenNode<Integer>
    {
        private final List<Object> list = new ArrayList<>();

        @Override
        public Object get(Integer key)
        {
            checkArgument(key >= 0);
            return key < list.size() ? list.get(key) : null;
        }

        @Override
        public void put(Integer key, Object value)
        {
            checkArgument(key >= 0);
            while (list.size() < key + 1) {
                list.add(null);
            }
            checkState(list.get(key) == null);
            list.set(key, value);
        }

        @Override
        public List<Object> build()
        {
            return immutableMapItems(list, UnflattenNode::build);
        }
    }

    public Map<String, Object> unflatten(Map<String, Object> flattened)
    {
        UnflattenMap root = new UnflattenMap();
        for (Map.Entry<String, Object> e : flattened.entrySet()) {
            UnflattenNode node = root;

            List<Object> keys = Splitter.on(delimiter).splitToList(checkNotEmpty(e.getKey())).stream()
                    .map(part -> {
                        if (part.contains(indexOpen)) {
                            checkState(part.endsWith(indexClose));
                            int pos = part.indexOf(indexOpen);
                            ImmutableList.Builder<Object> builder = ImmutableList.builder();
                            builder.add(part.substring(0, pos));
                            String idxStr = part.substring(pos + 1, part.length() - indexClose.length());
                            List<Integer> idxs = immutableMapItems(Splitter.on(indexClose + indexOpen).splitToList(idxStr), Integer::parseInt);
                            builder.addAll(idxs);
                            return builder.build();
                        }
                        else {
                            checkState(!part.contains(indexClose));
                            return ImmutableList.of(part);
                        }
                    })
                    .flatMap(List::stream)
                    .collect(toImmutableList());

            for (int i = 0; i <= keys.size() - 2; ++i) {
                Object key = keys.get(i);
                Object nextKey = keys.get(i + 1);

                if (nextKey instanceof String) {
                    node = (UnflattenMap) node.setDefault(key, UnflattenMap::new);
                }
                else if (nextKey instanceof Integer) {
                    node = (UnflattenList) node.setDefault(key, UnflattenList::new);
                }
                else {
                    throw new IllegalStateException(Objects.toString(nextKey));
                }
            }

            Object finalKey = keys.get(keys.size() - 1);
            node.put(finalKey, e.getValue());
        }
        return root.build();
    }

    private static final Flattening DEFAULT_INSTANCE = new Flattening();

    public static Map<String, Object> defaultFlatten(Map<String, Object> unflattened)
    {
        return DEFAULT_INSTANCE.flatten(unflattened);
    }

    public static Map<String, Object> defaultUnflatten(Map<String, Object> flattened)
    {
        return DEFAULT_INSTANCE.unflatten(flattened);
    }
}
