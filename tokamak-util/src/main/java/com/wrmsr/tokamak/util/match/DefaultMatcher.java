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
package com.wrmsr.tokamak.util.match;

import com.wrmsr.tokamak.util.match.pattern.CapturePattern;
import com.wrmsr.tokamak.util.match.pattern.EqualsPattern;
import com.wrmsr.tokamak.util.match.pattern.FilterPattern;
import com.wrmsr.tokamak.util.match.pattern.Pattern;
import com.wrmsr.tokamak.util.match.pattern.TypeOfPattern;
import com.wrmsr.tokamak.util.match.pattern.WithPattern;
import com.wrmsr.tokamak.util.match.pattern.matcher.PatternMatcher;

public class DefaultMatcher
        extends PatternMatcher
{
    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object)
    {
        return super.match(pattern, object);
    }

    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures)
    {
        return super.match(pattern, object, captures);
    }

    @Override
    public <T> Match<T> matchTypeOf(TypeOfPattern<T> pattern, Object object, Captures captures)
    {
        return super.matchTypeOf(pattern, object, captures);
    }

    @Override
    public <T> Match<T> matchWith(WithPattern<T> pattern, Object object, Captures captures)
    {
        return super.matchWith(pattern, object, captures);
    }

    @Override
    public <T> Match<T> matchCapture(CapturePattern<T> pattern, Object object, Captures captures)
    {
        return super.matchCapture(pattern, object, captures);
    }

    @Override
    public <T> Match<T> matchEquals(EqualsPattern<T> pattern, Object object, Captures captures)
    {
        return super.matchEquals(pattern, object, captures);
    }

    @Override
    public <T> Match<T> matchFilter(FilterPattern<T> pattern, Object object, Captures captures)
    {
        return super.matchFilter(pattern, object, captures);
    }
}
