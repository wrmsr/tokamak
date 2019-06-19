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
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class EquijoinNode
        extends AbstractNode
        implements JoinNode
{
    public enum Mode
    {
        INNER,
        LEFT,
        FULL
    }

    @Immutable
    public static final class Branch
    {
        private final Node node;
        private final FieldName field;

        public Branch(Node node, FieldName field)
        {
            this.node = node;
            this.field = field;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Branch branch = (Branch) o;
            return Objects.equals(node, branch.node) &&
                    Objects.equals(field, branch.field);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(node, field);
        }

        public Node getNode()
        {
            return node;
        }

        public FieldName getField()
        {
            return field;
        }
    }

    private final List<Branch> branches;
    private final Mode mode;

    public EquijoinNode(
            NodeName name,
            List<Branch> branches,
            Mode mode)
    {
        super(name);

        this.branches = ImmutableList.copyOf(branches);
        this.mode = mode;
    }

    public List<Branch> getBranches()
    {
        return branches;
    }

    public Mode getMode()
    {
        return mode;
    }

    @Override
    public List<Node> getChildren()
    {
        return branches.stream().map(b -> b.node).collect(toImmutableList());
    }

    @Override
    public <C, R> R accept(NodeVisitor<C, R> visitor, C context)
    {
        return visitor.visitEquijoinNode(this, context);
    }
}
