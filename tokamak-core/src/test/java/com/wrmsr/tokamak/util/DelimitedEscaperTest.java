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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

import java.util.List;

public class DelimitedEscaperTest
        extends TestCase
{
    public void testDelimitedEscaper()
            throws Throwable
    {
        DelimitedEscaper de = new DelimitedEscaper('.', '"', '\\', ImmutableSet.of());

        assertEquals(de.quote("abc"), "abc");
        assertEquals(de.quote("a.bc"), "\"a\\.bc\"");

        List<String> parts = ImmutableList.of("abc", "de.f", "g", "f");
        String delimited = de.delimit(parts);
        assertEquals(delimited, "abc.\"de\\.f\".g.f");
        List<String> undelimited = de.undelimit(delimited);
        assertEquals(undelimited, parts);
    }
}
