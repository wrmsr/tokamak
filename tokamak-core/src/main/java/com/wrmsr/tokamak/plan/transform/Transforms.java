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
package com.wrmsr.tokamak.plan.transform;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.visitor.NodeRewriter;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.NameGenerator;

import static com.google.common.base.Preconditions.checkState;

public final class Transforms
{
    private Transforms()
    {
    }

    public static Plan addScanNodeIdFields(Plan plan, Catalog catalog)
    {
        NameGenerator nameGenerator = new NameGenerator(plan.getNodeNames());
        return new Plan(plan.getRoot().accept(new NodeRewriter<Void>()
        {
            @Override
            public Node visitScanNode(ScanNode node, Void context)
            {
                Table table = catalog.lookupSchemaTable(node.getSchemaTable());
                if (!node.getIdFields().isEmpty()) {
                    checkState(table.getLayout().getPrimaryKeyFields().equals(node.getIdFields()));
                    return super.visitScanNode(node, context);
                }
                else if (node.getFields().keySet().containsAll(table.getLayout().getPrimaryKeyFields())) {
                    return new ScanNode(
                            node.getName(),
                            node.getSchemaTable(),
                            node.getFields(),
                            table.getLayout().getPrimaryKeyFields(),
                            node.getIdNodes(),
                            node.getInvalidations(),
                            node.getLinkageMasks(),
                            node.getLockOverride());
                }
                else {
                    ImmutableMap.Builder<String, Type> newFields = ImmutableMap.builder();
                    newFields.putAll(node.getFields());
                    table.getLayout().getPrimaryKeyFields().forEach(f -> {
                        if (!node.getFields().containsKey(f)) {
                            newFields.put(f, table.getRowLayout().getFields().get(f));
                        }
                    });

                    Node newScan = new ScanNode(
                            node.getName(),
                            node.getSchemaTable(),
                            newFields.build(),
                            table.getLayout().getPrimaryKeyFields(),
                            node.getIdNodes(),
                            node.getInvalidations(),
                            node.getLinkageMasks(),
                            node.getLockOverride());

                    return new ProjectNode(
                            nameGenerator.get(),
                            newScan,
                            Projection.only(node.getFields().keySet()));
                }
            }
        }, null));
    }
}
