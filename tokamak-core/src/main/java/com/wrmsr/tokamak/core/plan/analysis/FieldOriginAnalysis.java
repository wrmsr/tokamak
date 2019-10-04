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
import com.wrmsr.tokamak.core.plan.node.PGenerator;
import com.wrmsr.tokamak.core.plan.node.PGroupBy;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Immutable
public final class FieldOriginAnalysis
{
    /*
    TODO:
     - SUBFIELD TRACKING. INTO STRUCTS.
      - class PSearch extends PNode { private final SNode search
      - groupBy List<Struct<...>> ele origins?
    */

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
        STRONG,
        GENERATED,
    }

    public interface Nesting
    {
        final class Nested
                implements Nesting
        {
            private final String subfield;

            private Nested(String subfield)
            {
                this.subfield = checkNotNull(subfield);
            }

            public String getSubfield()
            {
                return subfield;
            }

            @Override
            public String toString()
            {
                return "Nested{" +
                        "subfield='" + subfield + '\'' +
                        '}';
            }
        }

        final class None
                implements Nesting
        {
            private static final None INSTANCE = new None();

            private None()
            {
            }

            @Override
            public String toString()
            {
                return "None{}";
            }
        }

        final class Unnested
                implements Nesting
        {
            private final String subfield;

            private Unnested(String subfield)
            {
                this.subfield = checkNotNull(subfield);
            }

            public String getSubfield()
            {
                return subfield;
            }

            @Override
            public String toString()
            {
                return "Unnested{" +
                        "subfield='" + subfield + '\'' +
                        '}';
            }
        }

        static Nested nested(String subfield)
        {
            return new Nested(subfield);
        }

        static None none()
        {
            return None.INSTANCE;
        }

        static Unnested unnested(String subfield)
        {
            return new Unnested(subfield);
        }
    }

    @Immutable
    public static final class Origination
    {
        private final NodeField sink;
        private final Optional<NodeField> source;
        private final Strength strength;
        private final Nesting nesting;

        private Origination(NodeField sink, Optional<NodeField> source, Strength strength, Nesting nesting)
        {
            this.sink = checkNotNull(sink);
            this.source = checkNotNull(source);
            this.strength = checkNotNull(strength);
            this.nesting = checkNotNull(nesting);
            source.ifPresent(s -> checkState(sink.node.getSources().contains(s.node)));
            if (!source.isPresent()) {
                checkArgument(strength == Strength.GENERATED);
                checkArgument(nesting instanceof Nesting.None);
            }
        }

        private Origination(NodeField sink, NodeField source, Strength strength, Nesting nesting)
        {
            this(sink, Optional.of(source), strength, nesting);
        }

        private Origination(NodeField sink)
        {
            this(sink, Optional.empty(), Strength.GENERATED, Nesting.none());
        }

        @Override
        public String toString()
        {
            return "Origination{" +
                    "sink=" + sink +
                    ", source=" + source +
                    ", strength=" + strength +
                    ", nesting=" + nesting +
                    '}';
        }

        public NodeField getSink()
        {
            return sink;
        }

        public Optional<NodeField> getSource()
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
            private void addGenerator(PGenerator node)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(NodeField.of(node, f))));
            }

            private void addSimpleSingleSource(PSingleSource node)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(NodeField.of(node, f), NodeField.of(node.getSource(), f), Strength.STRONG, Nesting.none())));
            }

            protected void visitSources(PNode node, Void context)
            {
                node.getSources().forEach(s -> s.accept(this, context));
            }

            @Override
            public Void visitCache(PCache node, Void context)
            {
                addSimpleSingleSource(node);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitCrossJoin(PCrossJoin node, Void context)
            {
                Strength str = node.getMode() == PCrossJoin.Mode.FULL ? Strength.STRONG : Strength.WEAK;
                node.getSources().forEach(s ->
                        s.getFields().getNames().forEach(f -> originations.add(new Origination(
                                NodeField.of(node, f), NodeField.of(s, f), str, Nesting.none()))));

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitEquiJoin(PEquiJoin node, Void context)
            {
                node.getBranches().forEach(b -> {
                    Strength str =
                            ((node.getMode() == PEquiJoin.Mode.LEFT && b == node.getBranches().get(0)) || node.getMode() == PEquiJoin.Mode.FULL) ?
                                    Strength.STRONG : Strength.WEAK;
                    b.getNode().getFields().getNames().forEach(f -> {
                        originations.add(new Origination(NodeField.of(node, f), NodeField.of(b.getNode(), f), str, Nesting.none()));
                    });
                });

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitFilter(PFilter node, Void context)
            {
                addSimpleSingleSource(node);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitGroupBy(PGroupBy node, Void context)
            {
                originations.add(new Origination(
                        NodeField.of(node, node.getListField())));
                node.getGroupFields().forEach(gf -> originations.add(new Origination(
                        NodeField.of(node, gf), NodeField.of(node.getSource(), gf), Strength.STRONG, Nesting.none())));

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitLookupJoin(PLookupJoin node, Void context)
            {
                checkState(false);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitProject(PProject node, Void context)
            {
                node.getProjection().getInputsByOutput().forEach((o, i) -> {
                    if (i instanceof PProjection.FieldInput) {
                        PProjection.FieldInput fi = (PProjection.FieldInput) i;
                        originations.add(new Origination(NodeField.of(node, o), NodeField.of(node.getSource(), fi.getField()), Strength.STRONG, Nesting.none()));
                    }
                });

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitScan(PScan node, Void context)
            {
                addGenerator(node);
                return null;
            }

            @Override
            public Void visitState(PState node, Void context)
            {
                addSimpleSingleSource(node);
                return super.visitState(node, context);
            }

            @Override
            public Void visitStruct(PStruct node, Void context)
            {
                checkState(false);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitUnion(PUnion node, Void context)
            {
                checkState(false);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitUnnest(PUnnest node, Void context)
            {
                checkState(false);

                visitSources(node, context);
                return null;
            }

            @Override
            public Void visitValues(PValues node, Void context)
            {
                addGenerator(node);
                return null;
            }
        }, null);

        return new FieldOriginAnalysis(originations);
    }
}
