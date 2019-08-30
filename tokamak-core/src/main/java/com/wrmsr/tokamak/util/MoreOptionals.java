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

package com.wrmsr.tokamak.util;

import java.util.OptionalInt;
import java.util.function.IntFunction;

public final class MoreOptionals
{
    private MoreOptionals()
    {
    }

    public static OptionalInt mapOptional(OptionalInt value, IntFunction<Integer> fn)
    {
        if (value.isPresent()) {
            Integer ret = fn.apply(value.getAsInt());
            return ret == null ? OptionalInt.empty() : OptionalInt.of(ret);
        }
        else {
            return OptionalInt.empty();
        }
    }

    public static OptionalInt sumOptionals(OptionalInt left, OptionalInt right)
    {
        if (left.isPresent() && right.isPresent()) {
            return OptionalInt.of(left.getAsInt() + right.getAsInt());
        }
        else {
            return OptionalInt.empty();
        }
    }

    public static OptionalInt sumOptionals(Iterable<OptionalInt> values)
    {
        int sum = 0;
        for (OptionalInt value : values) {
            if (!value.isPresent()) {
                return OptionalInt.empty();
            }
            sum += value.getAsInt();
        }
        return OptionalInt.of(sum);
    }
}
