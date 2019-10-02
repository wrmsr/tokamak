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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PBuildStruct
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;

    @JsonCreator
    public PBuildStruct(
            @JsonProperty("name") String name,
            @JsonProperty("source") PNode source)
    {
        super(name);
        this.source = checkNotNull(source);
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @Override
    public FieldCollection getFields()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitBuildStruct(this, context);
    }
}
