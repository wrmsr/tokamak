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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Set;

@Immutable
public final class CrossJoinNode
        extends AbstractNode
        implements JoinNode
{
    public enum Mode
    {
        INNER,
        FULL
    }

    private final List<Node> sources;

    public CrossJoinNode(
            String name,
            List<Node> sources,
            Mode mode)
    {
        super(name);

        this.sources = ImmutableList.copyOf(sources);

        checkInvariants();
    }

    @Override
    public Set<Node> getSources()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCrossJoinNode(this, context);
    }
}
