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
import com.wrmsr.tokamak.util.match.Property;
import com.wrmsr.tokamak.util.match.pattern.Pattern;
import junit.framework.TestCase;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

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

    private static final class IntLink
    {
        private final int value;
        private final Optional<IntLink> next;

        public IntLink(int value, Optional<IntLink> next)
        {
            this.value = value;
            this.next = checkNotNull(next);
        }

        public static Property<IntLink, IntLink> next()
        {
            return Property.ofOptional(l -> l.next);
        }
    }

    public void testLinkMatch()
            throws Exception
    {
        Capture<IntLink> capture0 = Capture.newCapture();
        Capture<IntLink> capture1 = Capture.newCapture();
        Capture<Integer> capture2 = Capture.newCapture();
        Capture<Integer> capture3 = Capture.newCapture();

        IntLink link = new IntLink(4, Optional.of(new IntLink(5, Optional.empty())));

        Pattern<IntLink> pattern = Pattern.typeOf(IntLink.class).capturedAs(capture0)
                .with(IntLink.next().matching(
                        Pattern.typeOf(IntLink.class).capturedAs(capture1)));

        Match<IntLink> match = new DefaultMatcher().match(pattern, link);
        System.out.println(match);

        pattern = Pattern.typeOf(IntLink.class).capturedAs(capture0)
                .with(IntLink.next().matching(
                        Pattern.typeOf(IntLink.class).capturedAs(capture1).with(
                                Property.<IntLink, Integer>of(l -> l.value).matching(v -> v >= 5))));

        match = new DefaultMatcher().match(pattern, link);
        System.out.println(match);

        pattern = Pattern.typeOf(IntLink.class).capturedAs(capture0)
                .with(IntLink.next().matching(
                        Pattern.typeOf(IntLink.class).capturedAs(capture1).with(
                                Property.<IntLink, Integer>of(l -> l.value).matching(v -> v >= 6))));

        match = new DefaultMatcher().match(pattern, link);
        System.out.println(match);
    }
}
