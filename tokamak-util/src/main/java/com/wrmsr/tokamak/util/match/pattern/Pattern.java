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

import com.wrmsr.tokamak.util.match.Capture;
import com.wrmsr.tokamak.util.match.Captures;
import com.wrmsr.tokamak.util.match.Match;
import com.wrmsr.tokamak.util.match.PropertyPatternPair;
import com.wrmsr.tokamak.util.match.pattern.matcher.PatternMatcher;
import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Pattern<T>
{
    private final Optional<Pattern<?>> next;

    protected Pattern(Optional<Pattern<?>> next)
    {
        this.next = checkNotNull(next);
    }

    protected Pattern()
    {
        this(Optional.empty());
    }

    protected Pattern(Pattern<?> next)
    {
        this(Optional.of(next));
    }

    public Optional<Pattern<?>> getNext()
    {
        return next;
    }

    public static Pattern<Object> any()
    {
        return typeOf(Object.class);
    }

    public static <T> Pattern<T> typeOf(Class<T> cls)
    {
        return new TypeOfPattern<>(cls);
    }

    public Pattern<T> capturedAs(Capture<T> capture)
    {
        return new CapturePattern<>(capture, this);
    }

    public Pattern<T> matching(Predicate<? super T> predicate)
    {
        return new FilterPattern<>(predicate, Optional.of(this));
    }

    public Pattern<T> with(PropertyPatternPair<? super T, ?> pattern)
    {
        return new WithPattern<>(pattern, Optional.of(this));
    }

    public abstract <R, C> R accept(PatternVisitor<R, C> visitor, C context);

    public abstract Match<T> accept(PatternMatcher matcher, Object object, Captures captures);
}
