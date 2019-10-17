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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

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

    public static Term apply(Map<Var, Term> s, Term t)
    {
        if (t instanceof Con) {
            return t;
        }
        else if (t instanceof App) {
            return new App(apply(s, ((App) t).left), apply(s, ((App) t).right));
        }
        else if (t instanceof Fun) {
            return new Fun(
                    ((Fun) t).args.stream().map(a -> apply(s, a)).collect(toImmutableList()),
                    apply(s, ((Fun) t).ret));
        }
        else if (t instanceof Var) {
            return s.getOrDefault(t, t);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(t));
        }
    }

    public static List<Term> apply(Map<Var, Term> s, Iterable<Term> ts)
    {
        return StreamSupport.stream(ts.spliterator(), false)
                .map(t -> apply(s, t))
                .collect(toImmutableList());
    }

    public static Term unify(Term x, Term y)
    {
        if (x instanceof App && y instanceof App) {
            Term s1 = unify(((App) x).left, ((App) y).left);
            Term s2 = unify(
                    apply(s1, ((App) x).right),
                    apply(s1, ((App) y).right));
        }

    }
}
