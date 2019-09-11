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
package com.wrmsr.tokamak.parser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.parser.tree.Relation;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SubqueryRelation;
import com.wrmsr.tokamak.parser.tree.TableName;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import com.wrmsr.tokamak.parser.tree.visitor.AstVisitor;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class AstPlanner
{
    private Optional<Catalog> catalog;
    private Optional<String> defaultSchema;

    public AstPlanner(Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        this.catalog = checkNotNull(catalog);
        this.defaultSchema = checkNotNull(defaultSchema);
    }

    public AstPlanner()
    {
        this(Optional.empty(), Optional.empty());
    }

    private Node planRelation(Relation relation)
    {
        return relation.accept(new AstVisitor<Node, Void>()
        {
            @Override
            public Node visitSubqueryRelation(SubqueryRelation treeNode, Void context)
            {
                return super.visitSubqueryRelation(treeNode, context);
            }

            @Override
            public Node visitTableName(TableName treeNode, Void context)
            {
                return super.visitTableName(treeNode, context);
            }
        }, null);
    }

    public Node plan(TreeNode treeNode)
    {
        return treeNode.accept(new AstVisitor<Node, Void>()
        {
            @Override
            public Node visitSelect(Select treeNode, Void context)
            {
                if (treeNode.getRelation().isPresent()) {
                    treeNode.getRelation().get().accept(new AstVisitor<Object, Object>()
                    {

                    }, null);
                }
                return new ScanNode(
                        "scan0",
                        SchemaTable.of("?", "t"),
                        ImmutableMap.of(),
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        ImmutableMap.of(),
                        ImmutableMap.of(),
                        Optional.empty());
            }
        }, null);
    }
}
