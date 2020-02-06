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
package com.wrmsr.tokamak.util.match.pattern;

import com.wrmsr.tokamak.util.match.Captures;
import com.wrmsr.tokamak.util.match.Match;
import com.wrmsr.tokamak.util.match.pattern.matcher.PatternMatcher;
import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FilterPattern<T>
        extends Pattern<T>
{
    private final Predicate<? super T> predicate;

    public FilterPattern(Predicate<? super T> predicate, Optional<Pattern<?>> next)
    {
        super(next);
        this.predicate = checkNotNull(predicate);
    }

    public Predicate<? super T> getPredicate()
    {
        return predicate;
    }

    @Override
    public <R, C> R accept(PatternVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilter(this, context);
    }

    @Override
    public Match<T> accept(PatternMatcher matcher, Object object, Captures captures)
    {
        return matcher.matchFilter(this, object, captures);
    }
}
