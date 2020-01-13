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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Lists.newArrayList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.toArrayList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

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

        @Override
        public String toString()
        {
            return "Var{" +
                    "num=" + num +
                    '}';
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

        @Override
        public String toString()
        {
            return "Con{" +
                    "value=" + value +
                    '}';
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

        @Override
        public String toString()
        {
            return "App{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
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

        @Override
        public String toString()
        {
            return "Fun{" +
                    "args=" + args +
                    ", ret=" + ret +
                    '}';
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

        @Override
        public String toString()
        {
            return "Constraint{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
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
                    immutableMapItems(((Fun) t).args, a -> apply(s, a)),
                    apply(s, ((Fun) t).ret));
        }
        else if (t instanceof Var) {
            return s.getOrDefault(t, t);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(t));
        }
    }

    public static final class UnificationError
            extends RuntimeException
    {
        private static final long serialVersionUID = 1430529479337446669L;

        private final Term x;
        private final Term y;

        public UnificationError(Term x, Term y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString()
        {
            return "UnificationError{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static Map<Var, Term> unify(Term x, Term y)
    {
        if (x instanceof App && y instanceof App) {
            Map<Var, Term> s1 = unify(((App) x).left, ((App) y).left);
            Map<Var, Term> s2 = unify(apply(s1, ((App) x).right), apply(s1, ((App) y).right));
            return compose(s2, s1);
        }
        else if (x instanceof Con && y instanceof Con && x.equals(y)) {
            return empty();
        }
        else if (x instanceof Fun && y instanceof Fun) {
            checkState(((Fun) x).args.size() != ((Fun) y).args.size());
            Map<Var, Term> s1 = solve(IntStream.range(0, ((Fun) x).args.size()).boxed()
                    .map(i -> new Constraint(((Fun) x).args.get(i), ((Fun) y).args.get(i)))
                    .collect(toImmutableList()));
            Map<Var, Term> s2 = unify(apply(s1, ((Fun) x).ret), apply(s1, ((Fun) y).ret));
            return compose(s2, s1);
        }
        else if (x instanceof Var) {
            return bind((Var) x, y);
        }
        else if (y instanceof Var) {
            return bind((Var) y, x);
        }
        else {
            throw new UnificationError(x, y);
        }
    }

    public static Map<Var, Term> solve(Iterable<Constraint> xs)
    {
        Map<Var, Term> mgu = empty();
        List<Constraint> cs = newArrayList(xs);
        while (!cs.isEmpty()) {
            Constraint c = cs.remove(cs.size() - 1);
            Map<Var, Term> s = unify(c.left, c.right);
            mgu = compose(s, mgu);
            cs = cs.stream().map(nc -> new Constraint(apply(s, nc.left), apply(s, nc.right))).collect(toArrayList());
        }
        return mgu;
    }

    public static Map<Var, Term> bind(Var n, Term x)
    {
        if (n.equals(x)) {
            return empty();
        }
        else if (occursCheck(n, x)) {
            throw new IllegalStateException();
        }
        else {
            return ImmutableMap.of(n, x);
        }
    }

    public static boolean occursCheck(Var n, Term x)
    {
        return ftv(x).contains(n);
    }

    public static Set<Var> ftv(Term x)
    {
        if (x instanceof Con) {
            return ImmutableSet.of();
        }
        else if (x instanceof App) {
            return ImmutableSet.<Var>builder()
                    .addAll(ftv(((App) x).left))
                    .addAll(ftv(((App) x).right))
                    .build();
        }
        else if (x instanceof Fun) {
            return ImmutableSet.<Var>builder()
                    .addAll(((Fun) x).args.stream().flatMap(a -> ftv(a).stream()).collect(toImmutableSet()))
                    .addAll(ftv(((Fun) x).ret))
                    .build();
        }
        else if (x instanceof Var) {
            return ImmutableSet.of((Var) x);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(x));
        }
    }

    public static Map<Var, Term> empty()
    {
        return ImmutableMap.of();
    }

    public static Map<Var, Term> compose(Map<Var, Term> s1, Map<Var, Term> s2)
    {
        Map<Var, Term> s3 = immutableMapValues(s2, u -> apply(s1, u));
        return ImmutableMap.<Var, Term>builder()
                .putAll(s3)
                .putAll(s1.entrySet().stream().filter(e -> !s3.containsKey(e.getKey())).collect(toImmutableMap()))
                .build();
    }

    public static Var var()
    {
        return new Var();
    }

    public static <T> Con<T> con(T value)
    {
        return new Con<>(value);
    }

    public static App app(Term left, Term right)
    {
        return new App(left, right);
    }

    public static Fun fun(List<Term> args, Term ret)
    {
        return new Fun(args, ret);
    }

    public static Constraint constraint(Term left, Term right)
    {
        return new Constraint(left, right);
    }

    public void testInference()
            throws Throwable
    {
        Var a = var();
        Var b = var();
        Var c = var();
        Var d = var();
        Var e = var();
        Var f = var();
        Var g = var();
        Var h = var();
        Var retty = var();

        Term ty = fun(ImmutableList.of(a, b), retty);

        List<Constraint> constraints = ImmutableList.copyOf(new Constraint[] {
                // (TApp(a=TCon(s='Array'), b=TCon(s='Int32')), TApp(a=TCon(s='Array'), b=TVar(s='$d')))
                constraint(app(con("Array"), con("Int32")), app(con("Array"), d)),

                // (TVar(s='$e'), TCon(s='Int32')),
                constraint(e, con("Int32")),

                // (TCon(s='Int32'), TCon(s='Int32')),
                constraint(con("Int32"), con("Int32")),

                // (TVar(s='$f'), TCon(s='Int64')),
                constraint(f, con("Int64")),

                // (TVar(s='$d'), TCon(s='Int32')),
                constraint(d, con("Int32")),

                // (TVar(s='$a'), TApp(s='Array'), b=TVar(s='$g'))),
                constraint(a, app(con("Array"), g)),

                // (TCon(s='Int32'), TCon(s='Int32')),
                constraint(con("Int32"), con("Int32")),

                // (TVar(s='$b'), TApp(s='Array'), b=TVar(s='$h'))),
                constraint(b, app(con("Array"), h)),

                // (TCon(s='Int32'), TCon(s='Int32')),
                constraint(con("Int32"), con("Int32")),

                // (TVar(s='$g'), TVar(s='$h')),
                constraint(g, h),

                // (TVar(s='$c'), TVar(s='$h')),
                constraint(c, h),

                // (TVar(s='$h'), TVar(s='$c')),
                constraint(h, c),

                // (TVar(s='$h'), TVar(s='$retty'))
                constraint(h, retty),
        });

        Map<Var, Term> mgu = solve(constraints);

        Term inferTy = apply(mgu, ty);

        System.out.println(inferTy);
    }
}
