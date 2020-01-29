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

import java.util.Objects;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class VField
        implements VNode
{
    private final String field;

    @JsonCreator
    public VField(
            @JsonProperty("field") String field)
    {
        this.field = checkNotEmpty(field);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        VField vField = (VField) o;
        return Objects.equals(field, vField.field);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field);
    }

    @Override
    public String toString()
    {
        return "Field{" +
                "field='" + field + '\'' +
                '}';
    }

    @JsonProperty("field")
    public String getField()
    {
        return field;
    }

    @Override
    public <R, C> R accept(VNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitField(this, context);
    }
}
