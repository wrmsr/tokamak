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

import com.wrmsr.tokamak.util.match.pattern.EqualsPattern;
import com.wrmsr.tokamak.util.match.pattern.FilterPattern;
import com.wrmsr.tokamak.util.match.pattern.Pattern;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Property<F, T>
{
    private final Function<? super F, Optional<? extends T>> function;

    private Property(Function<? super F, Optional<? extends T>> function)
    {
        this.function = checkNotNull(function);
    }

    public static <F, T> Property<F, T> of(Function<? super F, ? extends T> function)
    {
        return new Property<>(v -> Optional.of(function.apply(v)));
    }

    public static <F, T> Property<F, T> ofOptional(Function<? super F, Optional<? extends T>> function)
    {
        return new Property<>(function);
    }

    public Function<? super F, Optional<? extends T>> getFunction()
    {
        return function;
    }

    public <R> PropertyPatternPair<F, R> matching(Pattern<R> pattern)
    {
        return PropertyPatternPair.of(this, pattern);
    }

    public PropertyPatternPair<F, T> capturedAs(Capture<T> capture)
    {
        @SuppressWarnings({"unchecked"})
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.capturedAs(capture));
    }

    public PropertyPatternPair<F, T> equalTo(T value)
    {
        return matching(new EqualsPattern<>(value, null));
    }

    public PropertyPatternPair<F, T> matching(Predicate<? super T> predicate)
    {
        return matching(new FilterPattern<>(predicate, null));
    }
}
