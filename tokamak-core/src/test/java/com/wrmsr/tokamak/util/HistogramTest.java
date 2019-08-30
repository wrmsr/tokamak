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

import junit.framework.TestCase;

public final class HistogramTest
    extends TestCase
{
    public void testHistogram()
    {
        Histogram h = new Histogram(1024);
        h.add(100);
        h.add(200);
        h.add(130);
        Histogram.Stats s = h.get();
        System.out.println(s);
    }
}
