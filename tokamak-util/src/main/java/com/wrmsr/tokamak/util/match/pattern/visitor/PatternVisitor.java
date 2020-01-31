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
package com.wrmsr.tokamak.util.match.pattern.visitor;

import com.wrmsr.tokamak.util.match.pattern.CapturePattern;
import com.wrmsr.tokamak.util.match.pattern.EqualsPattern;
import com.wrmsr.tokamak.util.match.pattern.FilterPattern;
import com.wrmsr.tokamak.util.match.pattern.Pattern;
import com.wrmsr.tokamak.util.match.pattern.TypeOfPattern;
import com.wrmsr.tokamak.util.match.pattern.WithPattern;

import java.util.Objects;

public abstract class PatternVisitor<R, C>
{
    public <T> R process(Pattern<T> pattern, C context)
    {
        return pattern.accept(this, context);
    }

    protected <T> R visitPattern(Pattern<T> pattern, C context)
    {
        throw new IllegalStateException(Objects.toString(pattern));
    }

    public <T> R visitCapture(CapturePattern<T> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public <T> R visitEquals(EqualsPattern<T> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public <T> R visitFilter(FilterPattern<T> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public <T> R visitTypeOf(TypeOfPattern<T> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public <T> R visitWith(WithPattern<T> pattern, C context)
    {
        return visitPattern(pattern, context);
    }
}
