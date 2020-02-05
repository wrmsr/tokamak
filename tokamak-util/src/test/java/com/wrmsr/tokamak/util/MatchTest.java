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

import com.wrmsr.tokamak.util.match.Capture;
import com.wrmsr.tokamak.util.match.DefaultMatcher;
import com.wrmsr.tokamak.util.match.Match;
import com.wrmsr.tokamak.util.match.pattern.Pattern;
import junit.framework.TestCase;

public class MatchTest
        extends TestCase
{
    public void testMatch()
            throws Exception
    {
        Capture<Integer> capture = Capture.newCapture();
        Pattern<Integer> pattern = Pattern.typeOf(Integer.class).capturedAs(capture);
        Match<Integer> match = new DefaultMatcher().match(pattern, 5);
        System.out.println(match);
    }
}
