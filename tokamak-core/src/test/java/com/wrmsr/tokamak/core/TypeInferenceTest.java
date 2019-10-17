/* * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.wrmsr.tokamak.core;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

public class TypeInferenceTest
        extends TestCase
{
    public interface Term
    {
    }

    public static final class Var
            implements Term
    {
        private static final AtomicLong count = new AtomicLong();

        private final long num;

        public Var()
        {
            this.num = count.getAndIncrement();
        }
    }

    public static final class Con<T>
            implements Term
    {
        private final T value;

        public Con(T value)
        {
            this.value = checkNotNull(value);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Con<?> con = (Con<?>) o;
            return Objects.equals(value, con.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(value);
        }
    }

    public static final class App
            implements Term
    {
        private final Term left;
        private final Term right;

        public App(Term left, Term right)
        {
            this.left = checkNotNull(left);
            this.right = checkNotNull(right);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            App app = (App) o;
            return Objects.equals(left, app.left) &&
                    Objects.equals(right, app.right);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(left, right);
        }
    }

    public static final class Fun
            implements Term
    {
        private final List<Term> args;
        private final Term ret;

        public Fun(List<Term> args, Term ret)
        {
            this.args = ImmutableList.copyOf(args);
            this.ret = checkNotNull(ret);
            this.args.forEach(t -> checkNotNull(t));
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Fun fun = (Fun) o;
            return Objects.equals(args, fun.args) &&
                    Objects.equals(ret, fun.ret);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(args, ret);
        }
    }

    public static final class Constraint
    {
        private final Term left;
        private final Term right;

        public Constraint(Term left, Term right)
        {
            this.left = checkNotNull(left);
            this.right = checkNotNull(right);
        }
    }

    public static Term apply(Term l, Term r)
    {

    }
}
