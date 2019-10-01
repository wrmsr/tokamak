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
package com.wrmsr.tokamak.core.tree.node.visitor;

import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TNode;

public class TraversalTNodeVisitor<R, C>
        extends TNodeVisitor<R, C>
{
    @Override
    protected R visitTreeNode(TNode treeNode, C context)
    {
        return null;
    }

    @Override
    public R visitAliasedRelation(TAliasedRelation treeNode, C context)
    {
        treeNode.getRelation().accept(this, context);

        return null;
    }

    @Override
    public R visitExpressionSelectItem(TExpressionSelectItem treeNode, C context)
    {
        treeNode.getExpression().accept(this, context);

        return null;
    }

    @Override
    public R visitFunctionCallExpression(TFunctionCallExpression treeNode, C context)
    {
        treeNode.getArgs().forEach(a -> a.accept(this, context));

        return null;
    }

    @Override
    public R visitQualifiedNameExpression(TQualifiedNameExpression treeNode, C context)
    {
        treeNode.getQualifiedName().accept(this, context);

        return null;
    }

    @Override
    public R visitSelect(TSelect treeNode, C context)
    {
        treeNode.getRelations().forEach(r -> r.accept(this, context));
        treeNode.getItems().forEach(i -> i.accept(this, context));
        treeNode.getWhere().ifPresent(w -> w.accept(this, context));

        return null;
    }

    @Override
    public R visitSubqueryRelation(TSubqueryRelation treeNode, C context)
    {
        treeNode.getSelect().accept(this, context);

        return null;
    }
}
