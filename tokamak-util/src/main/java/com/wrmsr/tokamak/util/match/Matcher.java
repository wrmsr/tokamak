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
import com.wrmsr.tokamak.util.match.pattern.TypeOfPattern;
import com.wrmsr.tokamak.util.match.pattern.WithPattern;
import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

public class Matcher<M> extends PatternVisitor<Match<M>, Matcher<M>.Context>
{
    public final class Context
    {

    }

    @Override
    public <T> Match<M> visitCapture(CapturePattern<T> pattern, Context context)
    {
        return super.visitCapture(pattern, context);
    }

    @Override
    public <T> Match<M> visitEquals(EqualsPattern<T> pattern, Context context)
    {
        return super.visitEquals(pattern, context);
    }

    @Override
    public <T> Match<M> visitFilter(FilterPattern<T> pattern, Context context)
    {
        return super.visitFilter(pattern, context);
    }

    @Override
    public <T> Match<M> visitTypeOf(TypeOfPattern<T> pattern, Context context)
    {
        return super.visitTypeOf(pattern, context);
    }

    @Override
    public <T> Match<M> visitWith(WithPattern<T> pattern, Context context)
    {
        return super.visitWith(pattern, context);
    }
}
