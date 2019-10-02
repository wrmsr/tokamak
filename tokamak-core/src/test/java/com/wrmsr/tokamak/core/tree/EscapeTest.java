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

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;

public class EscapeTest
        extends TestCase
{
    public void testEscape()
            throws Throwable
    {
        ImmutableMap.<String, String>builder()
                .put("abcd", "abcd")
                .build().forEach((u, e) -> {
            TreeStrings.Unescaped us = TreeStrings.unescaped(u);
            TreeStrings.Escaped es = us.escaped();
            TreeStrings.Unescaped rus = es.unescaped();
            assertEquals(e, es.getValue());
            assertEquals(u, rus.getValue());
        });
    }
}
