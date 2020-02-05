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
package com.wrmsr.tokamak.util.match.pattern.matcher;

import com.wrmsr.tokamak.util.match.Captures;
import com.wrmsr.tokamak.util.match.Match;
import com.wrmsr.tokamak.util.match.pattern.CapturePattern;
import com.wrmsr.tokamak.util.match.pattern.EqualsPattern;
import com.wrmsr.tokamak.util.match.pattern.FilterPattern;
import com.wrmsr.tokamak.util.match.pattern.Pattern;
import com.wrmsr.tokamak.util.match.pattern.TypeOfPattern;
import com.wrmsr.tokamak.util.match.pattern.WithPattern;

import java.util.Objects;

public abstract class PatternMatcher
{
    public <T> Match<T> match(Pattern<T> pattern, Object object)
    {
        return match(pattern, object, Captures.empty());
    }

    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures)
    {
        throw new IllegalStateException(Objects.toString(pattern));
    }

    public <T> Match<T> matchTypeOf(TypeOfPattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, object, captures);
    }

    public <T> Match<T> matchWith(WithPattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, object, captures);
    }

    public <T> Match<T> matchCapture(CapturePattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, object, captures);
    }

    public <T> Match<T> matchEquals(EqualsPattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, object, captures);
    }

    public <T> Match<T> matchFilter(FilterPattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, object, captures);
    }
}
