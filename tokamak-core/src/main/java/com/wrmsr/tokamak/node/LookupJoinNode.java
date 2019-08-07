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

import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Immutable
public final class LookupJoinNode
        extends AbstractNode
        implements InternalNode, JoinNode
{
    @Immutable
    public static final class Branch
    {
        private final Node node;
        private final String field;

        public Branch(Node node, String field)
        {
            this.node = node;
            this.field = field;
        }

        public Node getNode()
        {
            return node;
        }

        public String getField()
        {
            return field;
        }
    }

    public LookupJoinNode(
            String name,
            Node source,
            List<Branch> branches,
            Optional<String> sourceIdField)
    {
        super(name);

        checkInvariants();
    }

    @Override
    public List<Node> getSources()
    {
        throw new IllegalStateException();
    }

    @Override
    public Map<String, Type> getFields()
    {
        throw new IllegalStateException();
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitLookupJoinNode(this, context);
    }
}
