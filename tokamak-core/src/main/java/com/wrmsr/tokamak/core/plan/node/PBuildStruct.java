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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;

@Immutable
public final class PBuildStruct
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final Set<String> structFields;
    private final String structField;

    @JsonCreator
    public PBuildStruct(
            @JsonProperty("name") String name,
            @JsonProperty("source") PNode source,
            @JsonProperty("structFields") Iterable<String> structFields,
            @JsonProperty("structFields") String structField)
    {
        super(name);
        this.source = checkNotNull(source);
        this.structFields = ImmutableSet.copyOf(checkUnique(ImmutableList.copyOf(checkOrdered(structFields))));
        this.structField = checkNotEmpty(structField);
        this.structFields.forEach(f -> checkNotEmpty(f));
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("structFields")
    public Set<String> getStructFields()
    {
        return structFields;
    }

    @JsonProperty("structField")
    public String getStructField()
    {
        return structField;
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
