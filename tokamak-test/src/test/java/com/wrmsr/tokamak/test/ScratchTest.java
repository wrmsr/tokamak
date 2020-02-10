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
package com.wrmsr.tokamak.test;

import com.tdunning.math.stats.TDigest;
import junit.framework.TestCase;

import java.util.concurrent.ThreadLocalRandom;

public class ScratchTest
        extends TestCase
{
    public void testTDigest()
            throws Throwable
    {
        TDigest digest = TDigest.createDigest(100);

        for (int j = 0; j < 200; ++j) {
            if (ThreadLocalRandom.current().nextFloat() >= 0.5) {
                digest.add(ThreadLocalRandom.current().nextDouble(0, 1_000_000));
            }
            else {
                digest.add(ThreadLocalRandom.current().nextDouble(10_000_000, 11_000_000));
            }
        }

        digest.centroids().forEach(System.out::println);
    }
}
