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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.OutputTarget;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Immutable
public final class PersistNode
        extends StatefulNode
        implements SingleSourceNode
{
    private final Node source;
    private final List<OutputTarget> outputTargets;
    private final boolean denormalized;

    @JsonCreator
    public PersistNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("outputTargets") List<OutputTarget> outputTargets,
            @JsonProperty("denormalized") boolean denormalized,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, invalidations, linkageMasks, lockOverride);

        this.source = source;
        this.outputTargets = ImmutableList.copyOf(outputTargets);
        this.denormalized = denormalized;

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("outputTargets")
    public List<OutputTarget> getOutputTargets()
    {
        return outputTargets;
    }

    @JsonProperty("denormalized")
    public boolean isDenormalized()
    {
        return denormalized;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return source.getFields();
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        return source.getIdFieldSets();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitPersistNode(this, context);
    }
}
