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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.visitor.NodeRewriter;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.NameGenerator;

import static com.google.common.base.Preconditions.checkState;

public final class Transforms
{
    private Transforms()
    {
    }

    public static Plan addScanNodeIdFields(Plan plan, Catalog catalog)
    {
        return new Plan(plan.getRoot().accept(new NodeRewriter<Void>()
        {
            NameGenerator ng = new NameGenerator(plan.getNodeNames());
            @Override
            public Node visitScanNode(ScanNode node, Void context)
            {
                Table table = catalog.lookupSchemaTable(node.getSchemaTable());
                if (!node.getIdFields().isEmpty()) {
                    checkState(ImmutableSet.copyOf(table.getLayout().getPrimaryKey()).equals(node.getIdFields()));
                    return super.visitScanNode(node, context);
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }
        }, null));
    }
}
