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
import com.wrmsr.tokamak.core.plan.node.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class StateNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Optional<List<Set<String>>> idFields;
    private final List<WriterTarget> writerTargets;
    private final boolean denormalized;
    private final Map<String, Invalidation> invalidations;
    private final Map<String, LinkageMask> linkageMasks;
    private final Optional<LockOverride> lockOverride;

    @JsonCreator
    public StateNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("idFields") Optional<List<Set<String>>> idFields,
            @JsonProperty("writerTargets") List<WriterTarget> writerTargets,
            @JsonProperty("denormalized") boolean denormalized,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
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
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("idFields")
    public Optional<List<Set<String>>> getIdFields()
    {
        return idFields;
    }

    @JsonProperty("writerTargets")
    public List<WriterTarget> getWriterTargets()
    {
        return writerTargets;
    }

    @JsonProperty("denormalized")
    public boolean isDenormalized()
    {
        return denormalized;
    }

    @JsonProperty("invalidations")
    public Map<String, Invalidation> getInvalidations()
    {
        return invalidations;
    }

    @JsonProperty("linkageMasks")
    public Map<String, LinkageMask> getLinkageMasks()
    {
        return linkageMasks;
    }

    @JsonProperty("lockOverride")
    public Optional<LockOverride> getLockOverride()
    {
        return lockOverride;
    }

    @Override
    public FieldCollection getFields()
    {
        return source.getFields();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitPersistNode(this, context);
    }
}
