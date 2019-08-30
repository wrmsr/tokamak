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
package com.wrmsr.tokamak.codec;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreOptionals.mapOptional;
import static com.wrmsr.tokamak.util.MoreOptionals.sumOptionals;

@Immutable
public final class Width
{
    private final int min;
    private final OptionalInt max;

    public Width(int min, OptionalInt max)
    {
        checkArgument(min >= 0);
        max.ifPresent(v -> checkArgument(v >= 0));
        if (max.isPresent()) {
            checkArgument(min <= max.getAsInt());
        }
        this.min = min;
        this.max = checkNotNull(max);
    }

    public static Width of(int min, OptionalInt max)
    {
        return new Width(min, max);
    }

    public static Width range(int min, int max)
    {
        return new Width(min, OptionalInt.of(max));
    }

    public static Width min(int min)
    {
        return new Width(min, OptionalInt.empty());
    }

    public static Width max(int max)
    {
        return new Width(0, OptionalInt.of(max));
    }

    public static Width fixed(int value)
    {
        return new Width(value, OptionalInt.of(value));
    }

    private static final Width UNBOUNDED = new Width(
            0,
            OptionalInt.empty());

    public static Width unbounded()
    {
        return UNBOUNDED;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Width width = (Width) o;
        return Objects.equals(min, width.min) &&
                Objects.equals(max, width.max);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(min, max);
    }

    @Override
    public String toString()
    {
        return "Width{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }

    public int getMin()
    {
        return min;
    }

    public OptionalInt getMax()
    {
        return max;
    }

    public OptionalInt getFixed()
    {
        if (max.isPresent() && min == max.getAsInt()) {
            return OptionalInt.of(min);
        }
        else {
            return OptionalInt.empty();
        }
    }

    public boolean isUnbounded()
    {
        return min == 0 && !max.isPresent();
    }

    @FunctionalInterface
    public interface IntFunction
    {
        int apply(int value);
    }

    public Width map(IntFunction fn)
    {
        return of(
                fn.apply(min),
                mapOptional(max, fn::apply));
    }

    public static Width sum(Iterable<Width> widths)
    {
        List<Width> lst = ImmutableList.copyOf(widths);
        return of(
                lst.stream().map(Width::getMin).reduce(0, Integer::sum),
                sumOptionals(lst.stream().map(Width::getMax).collect(toImmutableList())));
    }
}
