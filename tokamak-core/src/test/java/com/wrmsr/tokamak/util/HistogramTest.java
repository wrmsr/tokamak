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

import java.util.concurrent.ThreadLocalRandom;

public final class HistogramTest
        extends TestCase
{
    public void testHistogram()
            throws Throwable
    {
        Histogram h = new Histogram(1024);
        h.add(100);
        h.add(200);
        h.add(130);

        Histogram.Stats s = h.get();
        System.out.println(s);

        float t = 50.0f + ThreadLocalRandom.current().nextFloat() * 100.0f;
        for (float f = ThreadLocalRandom.current().nextFloat() * 3.0f; f < t;) {
            h.add(ThreadLocalRandom.current().nextFloat() * 100.0f);
            f += ThreadLocalRandom.current().nextFloat() * 3.0f;
            Thread.sleep(ThreadLocalRandom.current().nextInt(6));
        }

        s = h.get();
        System.out.println(s);

        for (int i = 0; i < 1200; ++i) {
            h.add(ThreadLocalRandom.current().nextFloat() * 100.0f);
        }

        s = h.get();
        System.out.println(s);

        for (int i = 0; i < 10000; ++i) {
            h.add(ThreadLocalRandom.current().nextFloat() * 10.0f);
        }

        s = h.get();
        System.out.println(s);
    }
}
