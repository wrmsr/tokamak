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
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.NameGenerator;

import static com.google.common.base.Preconditions.checkState;

public final class PTransforms
{
    private PTransforms()
    {
    }

    public static Plan addScanNodeIdFields(Plan plan, Catalog catalog)
    {
        NameGenerator nameGenerator = new NameGenerator(plan.getNodeNames());
        return new Plan(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitScan(PScan node, Void context)
            {
                Table table = catalog.getSchemaTable(node.getSchemaTable());
                if (!node.getIdFields().isEmpty()) {
                    checkState(table.getLayout().getPrimaryKeyFields().equals(node.getIdFields()));
                    return super.visitScan(node, context);
                }
                else if (node.getFields().getNames().containsAll(table.getLayout().getPrimaryKeyFields())) {
                    return new PScan(
                            node.getName(),
                            node.getAnnotations(),
                            node.getSchemaTable(),
                            node.getFields().getTypesByName(),
                            table.getLayout().getPrimaryKeyFields(),
                            node.getIdNodes());
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
                            node.getAnnotations(),
                            node.getSchemaTable(),
                            newFields.build(),
                            table.getLayout().getPrimaryKeyFields(),
                            node.getIdNodes());

                    return new PProject(
                            nameGenerator.get(),
                            node.getAnnotations(),
                            newScan,
                            PProjection.only(node.getFields().getNames()));
                }
            }
        }, null));
    }
}
