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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.config.Config;
import com.wrmsr.tokamak.util.config.ConfigProperty;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.arrayWithReplaced;
import static com.wrmsr.tokamak.util.MoreCollections.concatArrays;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public class ConfigTest
        extends TestCase
{
    public static class ThingConfig
            implements Config
    {
        public ThingConfig()
        {
        }

        private int beanInt;

        public int getBeanInt()
        {
            return beanInt;
        }

        @ConfigProperty
        public ThingConfig setBeanInt(int beanInt)
        {
            this.beanInt = beanInt;
            return this;
        }

        @ConfigProperty
        public int fieldInt;
    }

    public void testConfig()
            throws Throwable
    {
        Map map = ImmutableMap.of(
                "beanInt", 1
        );

        ThingConfig cfg = Json.readValue(Json.writeValue(map), ThingConfig.class);
        System.out.println(cfg);
    }

    @SuppressWarnings({"unchecked"})
    public static class Flattening
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

        private static final class UnflattenMap
        {
            private final Map<String, Object> map = new LinkedHashMap<>();
        }

        private static final class UnflattenList
        {
            private final List<Object> list = new ArrayList<>();
        }

        public Map<String, Object> unflatten(Map<String, Object> flattened)
        {
            UnflattenMap root = new UnflattenMap();
            for (Map.Entry<String, Object> e : flattened.entrySet()) {
                UnflattenMap node = root;
                for (String k : Splitter.on(delimiter).split(checkNotEmpty(e.getKey()))) {
                    checkNotEmpty(k);
                    if (k.contains(indexOpen)) {
                        checkState(k.endsWith(indexClose));
                        int p = k.indexOf(indexOpen);
                        int i = Integer.parseInt(k.substring(p, k.length() - indexClose.length()));
                        k = k.substring(p);
                    }
                    else {
                        checkState(!k.contains(indexClose));
                    }
                }
            }
            return root.map;
        }
    }

    public void testFlattening()
    {
        Flattening x = new Flattening();

        Map<String, Object> m = ImmutableMap.of(
                "a", 1,
                "b", ImmutableMap.of(
                        "c", 2
                ),
                "d", ImmutableList.of(
                        "e",
                        ImmutableMap.of(
                                "f", 3
                        )
                )
        );

        Map<String, Object> f = x.flatten(m);

        System.out.println(f);

        Map<String, Object> uf = x.unflatten(f);

        System.out.println(uf);
    }
}
