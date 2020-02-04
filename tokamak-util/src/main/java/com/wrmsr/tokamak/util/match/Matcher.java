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
import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public class Matcher<M>
        extends PatternVisitor<Match<M>, Matcher<M>.Context>
{
    public final class Context
    {
        private final Object object;
        private final Captures captures;

        public Context(Object object, Captures captures)
        {
            this.object = checkNotNull(object);
            this.captures = checkNotNull(captures);
        }
    }

    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures)
    {
        return match(pattern, new Context(object, captures));
    }

    public <T> Match<T> match(Pattern<T> pattern, Context context)
    {
        if (pattern.getNext().isPresent()) {
            Match<?> match = match(pattern.getNext().get(), context);
            return match.flatMap((value) -> pattern.accept(this, new Context(value, match.getCaptures())));
        }
        else {
            return pattern.accept(this, context);
        }
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
