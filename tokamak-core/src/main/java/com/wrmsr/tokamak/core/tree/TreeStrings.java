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
package com.wrmsr.tokamak.core.tree;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.box.Box;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TreeStrings
{
    /*
    TODO:
     - unicode literals: \\u+0123
    */

    private TreeStrings()
    {
    }

    public static final Set<Character> TRIPLE_QUOTE_CHARS = ImmutableSet.copyOf(new Character[] {
            '\'',
            '\n',
            '\r',
    });

    public static final Character ESCAPE_CHAR = '\'';

    public static final BiMap<Character, Character> ESCAPED_BY_UNESCAPED_MAP = ImmutableBiMap.<Character, Character>builder()
            .put('\'', '\'')
            .put('\n', 'n')
            .put('\r', 'r')
            .put(ESCAPE_CHAR, ESCAPE_CHAR)
            .build();

    public static final class Escaped
            extends Box<String>
    {
        private Escaped(String value)
        {
            super(checkNotNull(value));
        }

        public boolean shouldTripleQuote()
        {
            return TRIPLE_QUOTE_CHARS.stream().anyMatch(c -> value.indexOf(c) >= 0);
        }

        public String quoted()
        {
            return shouldTripleQuote() ? "'''" + value + "'''" : "'" + value + "'";
        }

        public Unescaped unescaped()
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.length(); ++i) {
                if (value.charAt(i) == ESCAPE_CHAR) {
                    sb.append(checkNotNull(ESCAPED_BY_UNESCAPED_MAP.inverse().get(value.charAt(++i))));
                }
                else {
                    sb.append(value.charAt(i));
                }
            }
            return new Unescaped(sb.toString());
        }
    }

    public static final class Unescaped
            extends Box<String>
    {
        private Unescaped(String value)
        {
            super(checkNotNull(value));
        }

        public Escaped escaped()
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.length(); ++i) {
                Character rep = ESCAPED_BY_UNESCAPED_MAP.get(value.charAt(i));
                if (rep != null) {
                    sb.append(ESCAPE_CHAR).append(rep);
                }
                else {
                    sb.append(rep);
                }
            }
            return new Escaped(sb.toString());
        }
    }

    public static Escaped escaped(String escaped)
    {
        return new Escaped(escaped);
    }

    public static Unescaped unescaped(String unescaped)
    {
        return new Unescaped(unescaped);
    }
}
