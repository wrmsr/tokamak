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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PState
        extends PAbstractNode
        implements PSingleSource
{
    public enum Denormalization
    {
        NONE,
        INPUT,
    }

    private final PNode source;
    private final List<PWriterTarget> writerTargets;
    private final Denormalization denormalization;
    private final Map<String, PInvalidation> invalidations;
    private final Map<String, PLinkageMask> linkageMasks;
    private final Optional<PLockOverride> lockOverride;

    @JsonCreator
    public PState(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("writerTargets") List<PWriterTarget> writerTargets,
            @JsonProperty("denormalization") Denormalization denormalization,
            @JsonProperty("invalidations") Map<String, PInvalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, PLinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<PLockOverride> lockOverride)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.writerTargets = ImmutableList.copyOf(writerTargets);
        this.denormalization = checkNotNull(denormalization);
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
        this.lockOverride = checkNotNull(lockOverride);

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("writerTargets")
    public List<PWriterTarget> getWriterTargets()
    {
        return writerTargets;
    }

    @JsonProperty("denormalization")
    public Denormalization getDenormalization()
    {
        return denormalization;
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
        return visitor.visitState(this, context);
    }
}
