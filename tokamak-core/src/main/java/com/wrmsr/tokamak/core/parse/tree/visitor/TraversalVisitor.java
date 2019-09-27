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

package com.wrmsr.tokamak.core.parse.tree.visitor;

import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.FunctionCallExpression;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;

public class TraversalVisitor<R, C>
        extends AstVisitor<R, C>
{
    @Override
    protected R visitTreeNode(TreeNode treeNode, C context)
    {
        return null;
    }

    @Override
    public R visitExpressionSelectItem(ExpressionSelectItem treeNode, C context)
    {
        treeNode.getExpression().accept(this, context);

        return null;
    }

    @Override
    public R visitFunctionCallExpression(FunctionCallExpression treeNode, C context)
    {
        treeNode.getArgs().forEach(a -> a.accept(this, context));

        return null;
    }

    @Override
    public R visitSelect(Select treeNode, C context)
    {
        treeNode.getItems().forEach(i -> i.accept(this, context));
        treeNode.getRelations().forEach(r -> r.accept(this, context));

        return null;
    }

    @Override
    public R visitSubqueryRelation(SubqueryRelation treeNode, C context)
    {
        treeNode.getSelect().accept(this, context);

        return null;
    }
}
