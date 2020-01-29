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

package com.wrmsr.tokamak.core.plan.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.core.plan.value.visitor.VNodeVisitor;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VConstant
        implements VNode
{
    private final @Nullable Object value;
    private final Type type;

    @JsonCreator
    public VConstant(
            @JsonProperty("value") @Nullable Object value,
            @JsonProperty("type") Type type)
    {
        this.value = value;
        this.type = checkNotNull(type);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        VConstant vConstant = (VConstant) o;
        return Objects.equals(value, vConstant.value) &&
                Objects.equals(type, vConstant.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value, type);
    }

    @Override
    public String toString()
    {
        return "Constant{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }

    @JsonProperty("value")
    @Nullable
    public Object getValue()
    {
        return value;
    }

    @JsonProperty("type")
    public Type getType()
    {
        return type;
    }

    @Override
    public <R, C> R accept(VNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitConstant(this, context);
    }
}
