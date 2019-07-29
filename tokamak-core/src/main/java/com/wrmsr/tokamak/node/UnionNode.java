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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class UnionNode
        extends AbstractNode
{
    private final Set<Node> sources;

    public UnionNode(
            String name,
            List<Node> sources)
    {
        super(name);

        checkArgument(!sources.isEmpty());
        Map<String, Type> fields = sources.get(0).getFields();
        for (int i = 1; i < sources.size(); ++i) {
            checkArgument(fields.equals(sources.get(i).getFields()));
        }

        this.sources = ImmutableSet.copyOf(sources);

        checkInvariants();
    }

    public Set<Node> getSources()
    {
        return sources;
    }

    @Override
    public Set<Node> getChildren()
    {
        return sources;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnionNode(this, context);
    }
}
