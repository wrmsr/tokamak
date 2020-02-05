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
    public R process(Pattern<?> pattern, C context)
    {
        return pattern.accept(this, context);
    }

    protected R visitPattern(Pattern<?> pattern, C context)
    {
        throw new IllegalStateException(Objects.toString(pattern));
    }

    public R visitCapture(CapturePattern<?> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public R visitEquals(EqualsPattern<?> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public R visitFilter(FilterPattern<?> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public R visitTypeOf(TypeOfPattern<?> pattern, C context)
    {
        return visitPattern(pattern, context);
    }

    public R visitWith(WithPattern<?> pattern, C context)
    {
        return visitPattern(pattern, context);
    }
}
