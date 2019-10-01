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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class PState
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final Optional<List<Set<String>>> idFields;
    private final List<PWriterTarget> writerTargets;
    private final boolean denormalized;
    private final Map<String, PInvalidation> invalidations;
    private final Map<String, PLinkageMask> linkageMasks;
    private final Optional<PLockOverride> lockOverride;

    @JsonCreator
    public PState(
            @JsonProperty("name") String name,
            @JsonProperty("source") PNode source,
            @JsonProperty("idFields") Optional<List<Set<String>>> idFields,
            @JsonProperty("writerTargets") List<PWriterTarget> writerTargets,
            @JsonProperty("denormalized") boolean denormalized,
            @JsonProperty("invalidations") Map<String, PInvalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, PLinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<PLockOverride> lockOverride)
    {
        super(name);

        this.source = checkNotNull(source);
        this.idFields = checkNotNull(idFields).map(s -> s.stream().map(ImmutableSet::copyOf).collect(toImmutableList()));
        this.writerTargets = ImmutableList.copyOf(writerTargets);
        this.denormalized = denormalized;
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
        this.lockOverride = checkNotNull(lockOverride);

        this.idFields.ifPresent(l -> l.forEach(s -> s.forEach(f -> checkArgument(this.getFields().contains(f)))));

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("idFields")
    public Optional<List<Set<String>>> getIdFields()
    {
        return idFields;
    }

    @JsonProperty("writerTargets")
    public List<PWriterTarget> getWriterTargets()
    {
        return writerTargets;
    }

    @JsonProperty("denormalized")
    public boolean isDenormalized()
    {
        return denormalized;
    }

    @JsonProperty("invalidations")
    public Map<String, PInvalidation> getInvalidations()
    {
        return invalidations;
    }

    @JsonProperty("linkageMasks")
    public Map<String, PLinkageMask> getLinkageMasks()
    {
        return linkageMasks;
    }

    @JsonProperty("lockOverride")
    public Optional<PLockOverride> getLockOverride()
    {
        return lockOverride;
    }

    @Override
    public FieldCollection getFields()
    {
        return source.getFields();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitPersistNode(this, context);
    }
}
