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

package com.wrmsr.tokamak.core.plan.analysis;

import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroupBy;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

@Immutable
public final class FieldOriginAnalysis
{
    @Immutable
    public static final class NodeField
    {
        private final PNode node;
        private final String field;

        public NodeField(PNode node, String field)
        {
            this.node = checkNotNull(node);
            this.field = checkNotNull(field);
        }

        @Override
        public String toString()
        {
            return "NodeField{" +
                    "node=" + node +
                    ", field='" + field + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            NodeField nodeField = (NodeField) o;
            return Objects.equals(node, nodeField.node) &&
                    Objects.equals(field, nodeField.field);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(node, field);
        }

        public PNode getNode()
        {
            return node;
        }

        public String getField()
        {
            return field;
        }
    }

    private final Map<PNode, Map<String, Set<NodeField>>> originSetsByFieldByNode;

    private FieldOriginAnalysis(Map<PNode, Map<String, Set<NodeField>>> originSetsByFieldByNode)
    {
        this.originSetsByFieldByNode = originSetsByFieldByNode.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> newImmutableSetMap(e.getValue())));
    }

    public static FieldOriginAnalysis analyze(Plan plan)
    {
        Map<PNode, Map<String, Set<NodeField>>> originSetsByFieldByNode = new LinkedHashMap<>();

        plan.getRoot().accept(new PNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitCacheNode(PCache node, Void context)
            {
                return super.visitCacheNode(node, context);
            }

            @Override
            public Void visitCrossJoinNode(PCrossJoin node, Void context)
            {
                return super.visitCrossJoinNode(node, context);
            }

            @Override
            public Void visitEquiJoinNode(PEquiJoin node, Void context)
            {
                return super.visitEquiJoinNode(node, context);
            }

            @Override
            public Void visitFilterNode(PFilter node, Void context)
            {
                return super.visitFilterNode(node, context);
            }

            @Override
            public Void visitGroupByNode(PGroupBy node, Void context)
            {
                return super.visitGroupByNode(node, context);
            }

            @Override
            public Void visitLookupJoinNode(PLookupJoin node, Void context)
            {
                return super.visitLookupJoinNode(node, context);
            }

            @Override
            public Void visitPersistNode(PState node, Void context)
            {
                return super.visitPersistNode(node, context);
            }

            @Override
            public Void visitProjectNode(PProject node, Void context)
            {
                return super.visitProjectNode(node, context);
            }

            @Override
            public Void visitScanNode(PScan node, Void context)
            {
                return super.visitScanNode(node, context);
            }

            @Override
            public Void visitUnionNode(PUnion node, Void context)
            {
                return super.visitUnionNode(node, context);
            }

            @Override
            public Void visitUnnestNode(PUnnest node, Void context)
            {
                return super.visitUnnestNode(node, context);
            }

            @Override
            public Void visitValuesNode(PValues node, Void context)
            {
                return super.visitValuesNode(node, context);
            }
        }, null);
    }
}
