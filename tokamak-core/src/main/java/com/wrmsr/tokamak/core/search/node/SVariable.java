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
package com.wrmsr.tokamak.core.search.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;
import com.wrmsr.tokamak.util.box.Box;
import com.wrmsr.tokamak.util.box.IntBox;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SVariable
        extends SNode
{
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NumberTarget.class, name = "number"),
            @JsonSubTypes.Type(value = NameTarget.class, name = "name"),
    })
    public interface Target
    {
        static NumberTarget of(int value)
        {
            return new NumberTarget(value);
        }

        static NameTarget of(String value)
        {
            return new NameTarget(value);
        }
    }

    public static final class NumberTarget
            extends IntBox
            implements Target
    {
        @JsonCreator
        public NumberTarget(
                @JsonProperty("value") int value)
        {
            super(value);
        }

        @JsonProperty("value")
        @Override
        public int getValue()
        {
            return super.getValue();
        }
    }

    public static final class NameTarget
            extends Box<String>
            implements Target
    {
        @JsonCreator
        public NameTarget(
                @JsonProperty("value") String value)
        {
            super(value);
        }

        @JsonProperty("value")
        @Override
        public String getValue()
        {
            return super.getValue();
        }
    }

    private final Target target;

    @JsonCreator
    public SVariable(
            @JsonProperty("target") Target target)
    {
        this.target = checkNotNull(target);
    }

    @JsonProperty("target")
    public Target getTarget()
    {
        return target;
    }

    @Override
    public <R, C> R accept(SNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitVariable(this, context);
    }
}
