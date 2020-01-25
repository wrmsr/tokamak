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

public interface OriginNesting
{
    /*
    TODO:
     - Group, Unnest, Struct, Extract
       - public Nesting inverse()
     - FUNCTION[_ARG]? generalized?
    */

    final class Nested
            implements OriginNesting
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
        public String toString()
        {
            return "Nested{" +
                    "sinkSubfield='" + sinkSubfield + '\'' +
                    '}';
        }
    }

    final class None
            implements OriginNesting
    {
        private static final None INSTANCE = new None();

        private None()
        {
        }

        @Override
        public String toString()
        {
            return "None{}";
        }
    }

    final class Unnested
            implements OriginNesting
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
        public String toString()
        {
            return "Unnested{" +
                    "sourceSubfield='" + sourceSubfield + '\'' +
                    '}';
        }
    }

    static Nested nested(String subfield)
    {
        return new Nested(subfield);
    }

    static None none()
    {
        return None.INSTANCE;
    }

    static Unnested unnested(String subfield)
    {
        return new Unnested(subfield);
    }
}
