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
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreOptionals.sumOptionals;

public interface Width
{
    /*
    TODO:
     - min?
    */

    OptionalInt getFixed();

    OptionalInt getMax();

    final class Unknown
            implements Width
    {
        private static final Unknown INSTANCE = new Unknown();

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getMax()
        {
            return OptionalInt.empty();
        }
    }

    static Width unknown()
    {
        return Unknown.INSTANCE;
    }

    final class Bounded
            implements Width
    {
        private final int max;

        public Bounded(int max)
        {
            checkArgument(max >= 0);
            this.max = max;
        }

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getMax()
        {
            return OptionalInt.of(max);
        }
    }

    static Width bounded(int max)
    {
        return new Bounded(max);
    }

    final class Fixed
            implements Width
    {
        private final int fixed;

        public Fixed(int fixed)
        {
            checkArgument(fixed >= 0);
            this.fixed = fixed;
        }

        @Override
        public OptionalInt getFixed()
        {
            return OptionalInt.of(fixed);
        }

        @Override
        public OptionalInt getMax()
        {
            return OptionalInt.of(fixed);
        }
    }

    static Width sum(Iterable<Width> widths)
    {
        List<Width> lst = ImmutableList.copyOf(widths);
        OptionalInt fixed = sumOptionals(lst.stream().map(Width::getFixed).collect(toImmutableList()));
        OptionalInt max = sumOptionals(lst.stream().map(Width::getMax).collect(toImmutableList()));
        if (fixed.isPresent()) {
            checkState(max.isPresent());
            checkState(max.getAsInt() == fixed.getAsInt());
            return new Fixed(fixed.getAsInt());
        }
        else if (max.isPresent()) {
            return new Bounded(max.getAsInt());
        }
        else {
            return unknown();
        }
    }
}

