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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroupBy;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
            checkState(node.getFields().contains(field));
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

        public static NodeField of(PNode node, String field)
        {
            return new NodeField(node, field);
        }
    }

    public enum Strength
    {
        WEAK,
        STRONG;
    }

    ;

    @Immutable
    public static final class Origination
    {
        private final NodeField sink;
        private final NodeField source;
        private final Strength strength;

        private Origination(NodeField sink, NodeField source, Strength strength)
        {
            this.sink = checkNotNull(sink);
            this.source = checkNotNull(source);
            this.strength = checkNotNull(strength);

            checkState(sink.node.getSources().contains(source.node));
        }

        @Override
        public String toString()
        {
            return "Origination{" +
                    "sink=" + sink +
                    ", source=" + source +
                    ", strength=" + strength +
                    '}';
        }

        public NodeField getSink()
        {
            return sink;
        }

        public NodeField getSource()
        {
            return source;
        }

        public Strength getStrength()
        {
            return strength;
        }
    }

    private final List<Origination> originations;

    private FieldOriginAnalysis(List<Origination> originations)
    {
        this.originations = ImmutableList.copyOf(originations);
    }

    public static FieldOriginAnalysis analyze(Plan plan)
    {
        List<Origination> originations = new ArrayList<>();

        plan.getRoot().accept(new PNodeVisitor<Void, Void>()
        {
            private void addSimple(PSingleSource node)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(NodeField.of(node, f), NodeField.of(node.getSource(), f), Strength.STRONG)));
            }

            @Override
            protected Void visitNode(PNode node, Void context)
            {
                node.getSources().forEach(s -> s.accept(this, context));
                return null;
            }

            @Override
            public Void visitCacheNode(PCache node, Void context)
            {
                addSimple(node);
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
                addSimple(node);
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
            public Void visitProjectNode(PProject node, Void context)
            {
                node.getProjection().getInputsByOutput().forEach((o, i) -> {
                    if (i instanceof PProjection.FieldInput) {
                        PProjection.FieldInput fi = (PProjection.FieldInput) i;
                        originations.add(new Origination(NodeField.of(node, o), NodeField.of(node.getSource(), fi.getField()), Strength.STRONG));
                    }
                });
                return super.visitProjectNode(node, context);
            }

            @Override
            public Void visitScanNode(PScan node, Void context)
            {
                return super.visitScanNode(node, context);
            }

            @Override
            public Void visitStateNode(PState node, Void context)
            {
                addSimple(node);
                return super.visitStateNode(node, context);
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

        return new FieldOriginAnalysis(originations);
    }
}
