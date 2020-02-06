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

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Genesis
{
    private Genesis()
    {
    }

    public boolean isLeaf()
    {
        return false;
    }

    public boolean isOpaque()
    {
        return false;
    }

    public static final class Direct
            extends Genesis
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
            extends Genesis
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
            extends Genesis
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
        public boolean isOpaque()
        {
            return true;
        }
    }

    public static External external()
    {
        return External.INSTANCE;
    }

    public static final class Scan
            extends Genesis
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
            extends Genesis
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
            extends Genesis
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
    }

    public static Join join(Join.Mode mode)
    {
        return new Join(mode);
    }

    public static final class Group
            extends Genesis
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
            extends Genesis
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
    }

    public static Nested nested(String sinkSubfield)
    {
        return new Nested(sinkSubfield);
    }

    public static final class Unnested
            extends Genesis
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
    }

    public static Unnested unnested(String sourceSubfield)
    {
        return new Unnested(sourceSubfield);
    }

    public static final class Function
            extends Genesis
    {
        private final boolean isOpaque;

        public Function(boolean isOpaque)
        {
            this.isOpaque = isOpaque;
        }

        @Override
        public boolean isOpaque()
        {
            return isOpaque;
        }
    }

    public static Function function(boolean isOpaque)
    {
        return new Function(isOpaque);
    }
}
