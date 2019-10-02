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

import com.wrmsr.tokamak.util.box.Box;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TreeStrings
{
    private TreeStrings()
    {
    }

    public static final class Escaped
            extends Box<String>
    {
        public Escaped(String value)
        {
            super(checkNotNull(value));
        }
    }

    public static final class Unescaped
            extends Box<String>
    {
        public Unescaped(String value)
        {
            super(checkNotNull(value));
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
