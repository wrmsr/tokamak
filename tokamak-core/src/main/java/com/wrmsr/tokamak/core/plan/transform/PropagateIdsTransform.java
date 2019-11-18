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
package com.wrmsr.tokamak.core.plan.transform;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PExtract;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeAnnotations;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnify;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValue;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.type.Type;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static com.google.common.collect.ImmutableList.sortedCopyOf;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class PropagateIdsTransform
{
    /*
    Adds *all possible id propagations and authoritatively sets all IdField annotations. Not everything will
    necessarily be successfully id'd.

    TODO:
     - scopes? everything needs concating scope id
     - volatility / redundancy compensation: for redundant fields on projects pick least volatile
      - prob using unify?
    */

    private PropagateIdsTransform()
    {
    }

    public static Plan propagateIds(Plan plan, Optional<Catalog> catalog)
    {
        return Plan.of(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            private PNode inherit(PSingleSource node, Void context, BiFunction<PNodeAnnotations, PNode, PNode> factory)
            {
                PNode source = process(node.getSource(), context);
                if (!source.getAnnotations().getFields().containsAnnotation(IdField.class)) {
                    return factory.apply(
                            node.getAnnotations().mapFields(fields -> fields
                                    .without(IdField.class)),
                            source);
                }
                else {
                    return factory.apply(
                            node.getAnnotations().mapFields(fields -> fields
                                    .without(IdField.class)
                                    .with(checkNotEmpty(source.getFields().getFieldNameSetsByAnnotationCls().get(IdField.class)), FieldAnnotation.id())),
                            source);
                }
            }

            @Override
            public PNode visitCache(PCache node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new PCache(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source));
            }

            @Override
            public PNode visitExtract(PExtract node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new PExtract(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source,
                                node.getSourceField(),
                                node.getStructMember(),
                                node.getOutputField()));
            }

            @Override
            public PNode visitFilter(PFilter node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new PFilter(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source,
                                node.getFunction(),
                                node.getArgs(),
                                node.getLinking()));
            }

            @Override
            public PNode visitGroup(PGroup node, Void context)
            {
                return new PGroup(
                        visitNodeName(node.getName(), context),
                        node.getAnnotations().mapFields(fields -> fields
                                .without(IdField.class)
                                .with(node.getKeyFields(), FieldAnnotation.id())),
                        process(node.getSource(), context),
                        node.getKeyFields(),
                        node.getListField());
            }

            @Override
            public PNode visitJoin(PJoin node, Void context)
            {
                List<PJoin.Branch> branches = node.getBranches().stream()
                        .map(b -> new PJoin.Branch(
                                process(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList());

                Set<String> idFields;
                if (branches.stream().allMatch(b -> b.getNode().getAnnotations().getFields().containsAnnotation(IdField.class))) {
                    idFields = branches.stream()
                            .flatMap(b -> checkNotEmpty(b.getNode().getFields().getFieldNameSetsByAnnotationCls().get(IdField.class)).stream())
                            .collect(toImmutableSet());
                }
                else {
                    idFields = ImmutableSet.of();
                }

                return new PJoin(
                        visitNodeName(node.getName(), context),
                        node.getAnnotations().mapFields(fields -> fields
                                .without(IdField.class)
                                .with(idFields, FieldAnnotation.id())),
                        branches,
                        node.getMode());
            }

            @Override
            public PNode visitLookup(PLookup node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public PNode visitOutput(POutput node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new POutput(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source,
                                node.getTargets()));
            }

            @Override
            public PNode visitProject(PProject node, Void context)
            {
                PNode source = process(node.getSource(), context);

                if (!source.getAnnotations().getFields().containsAnnotation(IdField.class)) {
                    return new PProject(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations().mapFields(fields -> fields
                                    .without(IdField.class)),
                            source,
                            node.getProjection());
                }

                ImmutableMap.Builder<String, PValue> newInputsByOutputBuilder = ImmutableMap.builder();
                ImmutableSet.Builder<String> idFieldsBuilder = ImmutableSet.builder();
                ImmutableSet.Builder<String> internalFieldsBuilder = ImmutableSet.builder();
                newInputsByOutputBuilder.putAll(node.getProjection());
                checkNotEmpty(source.getAnnotations().getFields().getKeySetsByAnnotationCls().get(IdField.class)).forEach(inputField -> {
                    String idField;
                    Set<String> outputSet = node.getProjection().getOutputSetsByInputField().get(inputField);
                    if (outputSet != null) {
                        // FIXME: do better
                        idField = sortedCopyOf(Ordering.natural(), checkNotEmpty(outputSet)).get(0);
                    }
                    else {
                        idField = plan.getFieldNameGenerator().get(inputField);
                        internalFieldsBuilder.add(idField);
                    }
                    idFieldsBuilder.add(idField);
                });
                Map<String, PValue> newInputsByOutput = newInputsByOutputBuilder.build();
                Set<String> idFields = idFieldsBuilder.build();
                Set<String> internalFields = internalFieldsBuilder.build();

                return new PProject(
                        visitNodeName(node.getName(), context),
                        node.getAnnotations().mapFields(fields -> fields
                                .without(IdField.class)
                                .with(idFields, FieldAnnotation.id())
                                .with(internalFields, FieldAnnotation.internal())),
                        source,
                        new PProjection(newInputsByOutput));
            }

            @Override
            public PNode visitScan(PScan node, Void context)
            {
                Table table = catalog.get().getSchemaTable(node.getSchemaTable());

                ImmutableMap.Builder<String, Type> newFieldsBuilder = ImmutableMap.builder();
                newFieldsBuilder.putAll(node.getFields().getTypesByName());
                table.getLayout().getPrimaryKeyFields().forEach(f -> {
                    if (!node.getFields().contains(f)) {
                        newFieldsBuilder.put(f, table.getRowLayout().getFields().getType(f));
                    }
                });
                Map<String, Type> newFields = newFieldsBuilder.build();

                return new PScan(
                        visitNodeName(node.getName(), context),
                        node.getAnnotations().mapFields(fields -> fields
                                .without(IdField.class)
                                .with(table.getLayout().getPrimaryKeyFields(), FieldAnnotation.id())
                                .with(newFields.keySet(), FieldAnnotation.internal())),
                        node.getSchemaTable(),
                        newFields,
                        PInvalidations.empty());
            }

            @Override
            public PNode visitScope(PScope node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public PNode visitScopeExit(PScopeExit node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public PNode visitSearch(PSearch node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public PNode visitState(PState node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new PState(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source,
                                node.getDenormalization(),
                                node.getInvalidations()));
            }

            @Override
            public PNode visitStruct(PStruct node, Void context)
            {
                return inherit(node, context, (annotations, source) ->
                        new PStruct(
                                visitNodeName(node.getName(), context),
                                annotations,
                                source,
                                node.getType(),
                                node.getInputFields(),
                                node.getOutputField()));
            }

            @Override
            public PNode visitUnify(PUnify node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public PNode visitUnion(PUnion node, Void context)
            {
                List<PNode> sources = node.getSources().stream().map(n -> process(n, context)).collect(toImmutableList());
                if (!sources.stream().allMatch(s -> s.getFields().containsAnnotation(IdField.class))) {
                    return new PUnion(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations().mapFields(fields -> fields
                                    .without(IdField.class)),
                            sources,
                            node.getIndexField());
                }

                Set<String> idFields = sources.stream()
                        .flatMap(s -> checkNotEmpty(s.getFields().getFieldNameSetsByAnnotationCls().get(IdField.class)).stream())
                        .collect(toImmutableSet());

                PNodeAnnotations annotations = node.getAnnotations().mapFields(fields -> fields
                        .without(IdField.class)
                        .with(idFields, FieldAnnotation.id()));

                String indexField;
                if (!node.getIndexField().isPresent()) {
                    indexField = plan.getFieldNameGenerator().get("index");
                    annotations = annotations.mapFields(fields -> fields
                            .with(indexField, FieldAnnotation.internal()));
                }
                else {
                    indexField = node.getIndexField().get();
                }
                annotations = annotations.mapFields(fields -> fields
                        .with(indexField, FieldAnnotation.id()));

                return new PUnion(
                        visitNodeName(node.getName(), context),
                        annotations,
                        sources,
                        Optional.of(indexField));
            }

            @Override
            public PNode visitUnnest(PUnnest node, Void context)
            {
                PNode source = process(node.getSource(), context);
                if (!source.getFields().containsAnnotation(IdField.class)) {
                    return new PUnnest(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations(),
                            source,
                            node.getListField(),
                            node.getUnnestedFields(),
                            node.getIndexField());
                }

                Set<String> idFields = checkNotEmpty(source.getFields().getFieldNameSetsByAnnotationCls().get(IdField.class));

                PNodeAnnotations annotations = node.getAnnotations().mapFields(fields -> fields
                        .without(IdField.class)
                        .with(idFields, FieldAnnotation.id()));

                String indexField;
                if (!node.getIndexField().isPresent()) {
                    indexField = plan.getFieldNameGenerator().get("index");
                    annotations = annotations.mapFields(fields -> fields
                            .with(indexField, FieldAnnotation.internal()));
                }
                else {
                    indexField = node.getIndexField().get();
                }
                annotations = annotations.mapFields(fields -> fields
                        .with(indexField, FieldAnnotation.id()));

                return new PUnnest(
                        visitNodeName(node.getName(), context),
                        annotations,
                        source,
                        node.getListField(),
                        node.getUnnestedFields(),
                        Optional.of(indexField));
            }

            @Override
            public PNode visitValues(PValues node, Void context)
            {
                PNodeAnnotations annotations = node.getAnnotations().mapFields(fields -> fields
                        .without(IdField.class));

                String indexField;
                if (!node.getIndexField().isPresent()) {
                    indexField = plan.getFieldNameGenerator().get("index");
                    annotations = annotations.mapFields(fields -> fields
                            .with(indexField, FieldAnnotation.internal()));
                }
                else {
                    indexField = node.getIndexField().get();
                }
                annotations = annotations.mapFields(fields -> fields
                        .with(indexField, FieldAnnotation.id()));

                return new PValues(
                        visitNodeName(node.getName(), context),
                        annotations,
                        node.getFields().getTypesByName(),
                        node.getValues(),
                        Optional.of(indexField),
                        node.getStrictness());
            }
        }, null));
    }
}
