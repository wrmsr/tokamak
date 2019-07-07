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
package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.api.OutputTarget;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
public final class PersistNode
        extends StatefulNode
        implements SingleSourceNode
{
    private final Node source;
    private final List<OutputTarget> outputTargets;
    private final boolean denormalized;

    public PersistNode(
            NodeName name,
            Node source,
            Iterable<OutputTarget> outputTargets,
            boolean denormalized,
            Map<NodeName, Invalidation> invalidations,
            Map<NodeName, LinkageMask> linkageMasks)
    {
        super(name, invalidations, linkageMasks);
        this.source = source;
        this.outputTargets = ImmutableList.copyOf(outputTargets);
        this.denormalized = denormalized;

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

    public List<OutputTarget> getOutputTargets()
    {
        return outputTargets;
    }

    public boolean isDenormalized()
    {
        return denormalized;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitPersistNode(this, context);
    }
}
