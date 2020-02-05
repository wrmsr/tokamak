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

import java.util.Optional;
import java.util.function.Function;

public class DefaultMatcher
        extends PatternMatcher
{
    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures)
    {
        if (pattern.getNext().isPresent()) {
            Match<?> match = match(pattern.getNext().get(), object, captures);
            return match.flatMap((value) -> pattern.accept(this, value, match.getCaptures()));
        }
        else {
            return pattern.accept(this, object, captures);
        }
    }

    @Override
    public <T> Match<T> matchTypeOf(TypeOfPattern<T> typeOfPattern, Object object, Captures captures)
    {
        Class<? extends T> expectedClass = typeOfPattern.getExpectedClass();
        if (expectedClass.isInstance(object)) {
            return Match.of(expectedClass.cast(object), captures);
        }
        else {
            return Match.empty();
        }
    }

    @Override
    public <T> Match<T> matchWith(WithPattern<T> withPattern, Object object, Captures captures)
    {
        Function<? super T, Optional<?>> property = withPattern.getProperty().getFunction();
        Optional<?> propertyValue = property.apply((T) object);
        Match<?> propertyMatch = propertyValue
                .map(value -> match(withPattern.getPattern(), value, captures))
                .orElse(Match.empty());
        return propertyMatch.map(ignored -> (T) object);
    }

    @Override
    public <T> Match<T> matchCapture(CapturePattern<T> capturePattern, Object object, Captures captures)
    {
        return Match.of((T) object, captures.addAll(Captures.ofNullable(capturePattern.capture(), (T) object)));
    }

    @Override
    public <T> Match<T> matchEquals(EqualsPattern<T> equalsPattern, Object object, Captures captures)
    {
        return Match.of((T) object, captures).filter(equalsPattern.getValue()::equals);
    }

    @Override
    public <T> Match<T> matchFilter(FilterPattern<T> filterPattern, Object object, Captures captures)
    {
        return Match.of((T) object, captures).filter(filterPattern.getPredicate());
    }
}
