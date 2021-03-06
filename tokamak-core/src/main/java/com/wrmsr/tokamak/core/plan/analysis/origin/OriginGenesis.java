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
package com.wrmsr.tokamak.core.plan.analysis.origin;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OriginGenesis
{
    private OriginGenesis()
    {
    }

    public boolean isLeaf()
    {
        return false;
    }

    public boolean isDeterministic()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object obj)
    {
        throw new IllegalStateException();
    }

    private static abstract class Nullary
            extends OriginGenesis
    {
        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            return true;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getClass());
        }
    }

    public static final class Direct
            extends Nullary
    {
        private static final Direct INSTANCE = new Direct();

        private Direct()
        {
        }
    }

    public static Direct direct()
    {
        return Direct.INSTANCE;
    }

    public static final class Constant
            extends Nullary
    {
        private static final Constant INSTANCE = new Constant();

        private Constant()
        {
        }

        @Override
        public boolean isLeaf()
        {
            return true;
        }
    }

    public static Constant constant()
    {
        return Constant.INSTANCE;
    }

    public static final class External
            extends Nullary
    {
        private static final External INSTANCE = new External();

        private External()
        {
        }

        @Override
        public boolean isLeaf()
        {
            return true;
        }

        @Override
        public boolean isDeterministic()
        {
            return true;
        }
    }

    public static External external()
    {
        return External.INSTANCE;
    }

    public static final class Scan
            extends Nullary
    {
        private static final Scan INSTANCE = new Scan();

        private Scan()
        {
        }

        @Override
        public boolean isLeaf()
        {
            return true;
        }
    }

    public static Scan scan()
    {
        return Scan.INSTANCE;
    }

    public static final class Values
            extends Nullary
    {
        private static final Values INSTANCE = new Values();

        private Values()
        {
        }

        @Override
        public boolean isLeaf()
        {
            return true;
        }
    }

    public static Values values()
    {
        return Values.INSTANCE;
    }

    public static final class Join
            extends OriginGenesis
    {
        public enum Mode
        {
            INNER,
            LEFT_PRIMARY,
            LEFT_SECONDARY,
            FULL,
            LOOKUP,
        }

        private final Mode mode;

        public Join(Mode mode)
        {
            this.mode = checkNotNull(mode);
        }

        public Mode getMode()
        {
            return mode;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Join join = (Join) o;
            return mode == join.mode;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(mode);
        }
    }

    public static Join join(Join.Mode mode)
    {
        return new Join(mode);
    }

    public static final class Group
            extends Nullary
    {
        private static final Group INSTANCE = new Group();

        private Group()
        {
        }
    }

    public static Group group()
    {
        return Group.INSTANCE;
    }

    public static final class Nested
            extends OriginGenesis
    {
        private final String sinkSubfield;

        private Nested(String sinkSubfield)
        {
            this.sinkSubfield = checkNotNull(sinkSubfield);
        }

        public String getSinkSubfield()
        {
            return sinkSubfield;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Nested nested = (Nested) o;
            return Objects.equals(sinkSubfield, nested.sinkSubfield);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(sinkSubfield);
        }
    }

    public static Nested nested(String sinkSubfield)
    {
        return new Nested(sinkSubfield);
    }

    public static final class Unnested
            extends OriginGenesis
    {
        private final String sourceSubfield;

        private Unnested(String sourceSubfield)
        {
            this.sourceSubfield = checkNotNull(sourceSubfield);
        }

        public String getSourceSubfield()
        {
            return sourceSubfield;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Unnested unnested = (Unnested) o;
            return Objects.equals(sourceSubfield, unnested.sourceSubfield);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(sourceSubfield);
        }
    }

    public static Unnested unnested(String sourceSubfield)
    {
        return new Unnested(sourceSubfield);
    }

    public static final class Function
            extends OriginGenesis
    {
        private final boolean isDeterministic;

        public Function(boolean isDeterministic)
        {
            this.isDeterministic = isDeterministic;
        }

        @Override
        public boolean isDeterministic()
        {
            return isDeterministic;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Function function = (Function) o;
            return isDeterministic == function.isDeterministic;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(isDeterministic);
        }
    }

    public static Function function(boolean isDeterministic)
    {
        return new Function(isDeterministic);
    }
}
