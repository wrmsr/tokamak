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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.box.Box;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TreeStrings
{
    private TreeStrings()
    {
    }

    public static final Set<Character> ESCAPED_CHARS = ImmutableSet.of(
            '\'',
            '\\'
    );

    public static final class Escaped
            extends Box<String>
    {
        private Escaped(String value)
        {
            super(checkNotNull(value));
        }

        public boolean shouldTripleQuote()
        {
            return value.contains("'");
        }

        public String quoted()
        {
            return shouldTripleQuote() ? "'''" + value + "'''" : "'" + value + "'";
        }

        public Unescaped unescaped()
        {

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
