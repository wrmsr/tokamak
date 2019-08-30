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

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreOptionals.mapOptional;
import static com.wrmsr.tokamak.util.MoreOptionals.sumOptionals;

public interface Width
{
    /*
    TODO:
     - monoid lol
     - god, is this just a Span<Integer>
      - span wastefully supports inexactly-comparable (via ABOVE/BELOW)
      - well min/max/fixed is.. stupid..
    */

    OptionalInt getMin();

    OptionalInt getMax();

    OptionalInt getFixed();

    @FunctionalInterface
    interface IntFunctor
    {
        int apply(int value);
    }

    Width map(IntFunctor fn);

    <R> R accept(Visitor<R> visitor);

    abstract class Visitor<R>
    {
        public R visitWidth(Width width)
        {
            throw new IllegalArgumentException(Objects.toString(width));
        }

        public R visitUnknown(Unknown width)
        {
            return visitWidth(width);
        }

        public R visitRange(Range width)
        {
            return visitWidth(width);
        }

        public R visitFixed(Fixed width)
        {
            return visitWidth(width);
        }
    }

    final class Unknown
            implements Width
    {
        private static final Unknown INSTANCE = new Unknown();

        @Override
        public OptionalInt getMin()
        {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getMax()
        {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.empty();
        }

        @Override
        public Width map(IntFunctor fn)
        {
            return this;
        }

        @Override
        public <R> R accept(Visitor<R> visitor)
        {
            return visitor.visitUnknown(this);
        }
    }

    final class Range
            implements Width
    {
        private final OptionalInt minValue;
        private final OptionalInt maxValue;

        public Range(OptionalInt minValue, OptionalInt maxValue)
        {
            checkArgument(checkNotNull(minValue).isPresent() || checkNotNull(maxValue).isPresent());
            if (minValue.isPresent() && maxValue.isPresent()) {
                checkArgument(minValue.getAsInt() < maxValue.getAsInt());
            }
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public OptionalInt getMin()
        {
            return minValue;
        }

        @Override
        public OptionalInt getMax()
        {
            return maxValue;
        }

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.empty();
        }

        @Override
        public Width map(IntFunctor fn)
        {
            return of(
                    mapOptional(minValue, fn::apply),
                    mapOptional(maxValue, fn::apply));
        }

        @Override
        public <R> R accept(Visitor<R> visitor)
        {
            return visitor.visitRange(this);
        }
    }

    final class Fixed
            implements Width
    {
        private final int value;

        public Fixed(int value)
        {
            checkArgument(value >= 0);
            this.value = value;
        }

        @Override
        public OptionalInt getMin()
        {
            return OptionalInt.of(value);
        }

        @Override
        public OptionalInt getMax()
        {
            return OptionalInt.of(value);
        }

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.of(value);
        }

        @Override
        public Width map(IntFunctor fn)
        {
            return new Fixed(fn.apply(value));
        }

        @Override
        public <R> R accept(Visitor<R> visitor)
        {
            return visitor.visitFixed(this);
        }
    }

    static Width unknown()
    {
        return Unknown.INSTANCE;
    }

    static Width of(OptionalInt min, OptionalInt max)
    {
        if (min.isPresent() && max.isPresent()) {
            if (min.getAsInt() == max.getAsInt()) {
                return new Fixed(min.getAsInt());
            }
            else {
                return new Range(min, max);
            }
        }
        else if (min.isPresent() || max.isPresent()) {
            return new Range(min, max);
        }
        else {
            return Unknown.INSTANCE;
        }
    }

    static Width of(int value)
    {
        return new Fixed(value);
    }

    static Width of(int min, int max)
    {
        return new Range(OptionalInt.of(min), OptionalInt.of(max));
    }

    static Width sum(Iterable<Width> widths)
    {
        List<Width> lst = ImmutableList.copyOf(widths);
        OptionalInt min = sumOptionals(lst.stream().map(Width::getMin).collect(toImmutableList()));
        OptionalInt max = sumOptionals(lst.stream().map(Width::getMax).collect(toImmutableList()));
        return of(min, max);
    }
}
