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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.NameGenerator;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class PTransforms
{
    private PTransforms()
    {
    }

    public static Plan addScanNodeIdFields(Plan plan, Catalog catalog)
    {
        // TODO: remove. generalize to addRequiredIdFields
        NameGenerator nameGenerator = new NameGenerator(plan.getNodeNames());
        return new Plan(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitScan(PScan node, Void context)
            {
                Table table = catalog.getSchemaTable(node.getSchemaTable());
                List<Field> existing = node.getFields().getFieldListsByAnnotationCls().get(IdField.class);
                if (existing != null) {
                    checkState(table.getLayout().getPrimaryKeyFields().equals(existing.stream().map(Field::getName).collect(toImmutableSet())));
                    return super.visitScan(node, context);
                }
                else if (node.getFields().getNames().containsAll(table.getLayout().getPrimaryKeyFields())) {
                    return new PScan(
                            node.getName(),
                            node.getAnnotations().mapFields(fields -> fields.overwriting(table.getLayout().getPrimaryKeyFields(), FieldAnnotation.id())),
                            node.getSchemaTable(),
                            node.getFields().getTypesByName(),
                            ImmutableList.of());
                }
                else {
                    ImmutableMap.Builder<String, Type> newFields = ImmutableMap.builder();
                    newFields.putAll(node.getFields().getTypesByName());
                    table.getLayout().getPrimaryKeyFields().forEach(f -> {
                        if (!node.getFields().contains(f)) {
                            newFields.put(f, table.getRowLayout().getFields().getType(f));
                        }
                    });

                    PNode newScan = new PScan(
                            node.getName(),
                            node.getAnnotations().mapFields(fields -> fields.overwriting(table.getLayout().getPrimaryKeyFields(), FieldAnnotation.id())),
                            node.getSchemaTable(),
                            newFields.build(),
                            ImmutableList.of());

                    return new PProject(
                            nameGenerator.get(),
                            node.getAnnotations(),
                            newScan,
                            PProjection.only(node.getFields().getNames()));
                }
            }
        }, null));
    }

    public static PNode addIdDistinguishingFields(Plan plan)
    {
        // TODO: fuse with scans above / remove / generalize
        return plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitUnion(PUnion node, Void context)
            {
                String indexField = plan.getFieldNameGenerator().get("index");
                if (!node.getIndexField().isPresent()) {
                    return new PUnion(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations().mapFields(f -> f.with(indexField, FieldAnnotation.internal())),
                            node.getSources().stream().map(n -> process(n, context)).collect(toImmutableList()),
                            Optional.of(indexField));
                }
                else {
                    return super.visitUnion(node, context);
                }
            }

            @Override
            public PNode visitUnnest(PUnnest node, Void context)
            {
                if (!node.getIndexField().isPresent()) {
                    String indexField = plan.getFieldNameGenerator().get("index");
                    return new PUnnest(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations().mapFields(f -> f.with(indexField, FieldAnnotation.internal())),
                            process(node.getSource(), context),
                            node.getListField(),
                            node.getUnnestedFields(),
                            Optional.of(indexField));
                }
                else {
                    return super.visitUnnest(node, context);
                }
            }

            @Override
            public PNode visitValues(PValues node, Void context)
            {
                if (!node.getIndexField().isPresent()) {
                    String indexField = plan.getFieldNameGenerator().get("index");
                    return new PValues(
                            visitNodeName(node.getName(), context),
                            node.getAnnotations().mapFields(f -> f.with(indexField, FieldAnnotation.internal())),
                            node.getFields().getTypesByName(),
                            node.getValues(),
                            Optional.of(indexField),
                            node.getStrictness());
                }
                else {
                    return super.visitValues(node, context);
                }
            }
        }, null);
    }
}
