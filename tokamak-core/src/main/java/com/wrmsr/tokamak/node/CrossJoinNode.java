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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;

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
    private final Mode mode;

    public CrossJoinNode(
            String name,
            List<Node> sources,
            Mode mode)
    {
        super(name);

        this.sources = ImmutableList.copyOf(sources);
        this.mode = mode;

        checkUnique(this.sources.stream()
                .flatMap(n -> n.getFields().keySet().stream())
                .collect(toImmutableList()));

        checkInvariants();
    }

    @Override
    public List<Node> getSources()
    {
        return sources;
    }

    public Mode getMode()
    {
        return mode;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCrossJoinNode(this, context);
    }
}
