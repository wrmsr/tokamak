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

package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public interface PValue
{
    final class Constant
            implements PValue
    {
        private final @Nullable Object value;

        @JsonCreator
        public Constant(
                @JsonProperty("value") @Nullable Object value)
        {
            this.value = value;
        }

        @JsonProperty("value")
        @Nullable
        public Object getValue()
        {
            return value;
        }
    }

    final class Field
            implements PValue
    {
        private final String field;

        @JsonCreator
        public Field(
                @JsonProperty("field") String field)
        {
            this.field = checkNotEmpty(field);
        }

        @JsonProperty("field")
        public String getField()
        {
            return field;
        }
    }

    final class Function
            implements PValue
    {
        private final PFunction function;
        private final List<PValue> args;

        @JsonCreator
        public Function(
                @JsonProperty("function") PFunction function,
                @JsonProperty("args") List<PValue> args)
        {
            this.function = checkNotNull(function);
            this.args = ImmutableList.copyOf(args);
            checkState(this.args.size() == function.getType().getParamTypes().size());
        }

        @JsonProperty("function")
        public PFunction getFunction()
        {
            return function;
        }

        @JsonProperty("args")
        public List<PValue> getArgs()
        {
            return args;
        }
    }

    static Constant constant(Object value)
    {
        return new Constant(value);
    }

    static Field field(String field)
    {
        return new Field(field);
    }

    static Function function(PFunction function, Iterable<PValue> args)
    {
        return new Function(function, ImmutableList.copyOf(args));
    }
}
