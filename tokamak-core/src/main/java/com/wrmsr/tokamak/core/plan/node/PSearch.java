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
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.type.Type;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PSearch
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final SNode search;
    private final String outputField;
    private final Type outputType;

    @JsonCreator
    public PSearch(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("search") SNode search,
            @JsonProperty("outputField") String outputField,
            @JsonProperty("outputType") Type outputType)
    {
        super(name, annotations);
        this.source = checkNotNull(source);
        this.search = checkNotNull(search);
        this.outputField = checkNotEmpty(outputField);
        this.outputType = checkNotNull(outputType);
        checkState(!source.getFields().contains(outputField));

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("search")
    public SNode getSearch()
    {
        return search;
    }

    @JsonProperty("outputField")
    public String getOutputField()
    {
        return outputField;
    }

    @JsonProperty("outputType")
    public Type getOutputType()
    {
        return outputType;
    }

    @Override
    public FieldCollection getFields()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitSearch(this, context);
    }
}
