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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;

@Immutable
public final class PState
        extends PAbstractNode
        implements PInvalidatable, PInvalidator, PSingleSource
{
    public enum Denormalization
    {
        NONE,
        INPUT,
    }

    private final PNode source;
    private final Denormalization denormalization;
    private final Map<String, Set<String>> linkageMasks;
    private final List<PInvalidation> invalidations;

    private FieldCollection fields;

    @JsonCreator
    public PState(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("denormalization") Denormalization denormalization,
            @JsonProperty("linkageMasks") Map<String, Set<String>> linkageMasks,
            @JsonProperty("invalidations") List<PInvalidation> invalidations,
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.denormalization = checkNotNull(denormalization);
        this.linkageMasks = newImmutableSetMap(linkageMasks);
        this.invalidations = ImmutableList.copyOf(invalidations);

        fields = source.getFields()
                .withOnlyTransitiveAnnotations()
                .withAnnotations(annotations.getFields());

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("denormalization")
    public Denormalization getDenormalization()
    {
        return denormalization;
    }

    @JsonProperty("linkageMasks")
    @Override
    public Map<String, Set<String>> getLinkageMasks()
    {
        return linkageMasks;
    }

    @JsonProperty("invalidations")
    public List<PInvalidation> getInvalidations()
    {
        return invalidations;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitState(this, context);
    }
}
