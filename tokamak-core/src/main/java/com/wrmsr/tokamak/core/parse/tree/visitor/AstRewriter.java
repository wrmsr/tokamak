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

import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Expression;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.FunctionCallExpression;
import com.wrmsr.tokamak.core.parse.tree.Identifier;
import com.wrmsr.tokamak.core.parse.tree.NullLiteral;
import com.wrmsr.tokamak.core.parse.tree.NumberLiteral;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Relation;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SelectItem;
import com.wrmsr.tokamak.core.parse.tree.StringLiteral;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class AstRewriter<C>
        extends AstVisitor<TreeNode, C>
{
    @Override
    protected TreeNode visitTreeNode(TreeNode treeNode, C context)
    {
        throw new IllegalStateException();
    }

    @Override
    public TreeNode visitAliasedRelation(AliasedRelation treeNode, C context)
    {
        return new AliasedRelation(
                (Relation) treeNode.getRelation().accept(this, context),
                treeNode.getAlias());
    }

    @Override
    public TreeNode visitAllSelectItem(AllSelectItem treeNode, C context)
    {
        return new AllSelectItem();
    }

    @Override
    public TreeNode visitExpressionSelectItem(ExpressionSelectItem treeNode, C context)
    {
        return new ExpressionSelectItem(
                (Expression) treeNode.getExpression().accept(this, context),
                treeNode.getLabel());
    }

    @Override
    public TreeNode visitFunctionCallExpression(FunctionCallExpression treeNode, C context)
    {
        return new FunctionCallExpression(
                treeNode.getName(),
                treeNode.getArgs().stream().map(a -> (Expression) a.accept(this, context)).collect(toImmutableList()));
    }

    @Override
    public TreeNode visitIdentifier(Identifier treeNode, C context)
    {
        return new Identifier(
                treeNode.getValue());
    }

    @Override
    public TreeNode visitNullLiteral(NullLiteral treeNode, C context)
    {
        return new NullLiteral();
    }

    @Override
    public TreeNode visitNumberLiteral(NumberLiteral treeNode, C context)
    {
        return new NumberLiteral(
                treeNode.getValue());
    }

    @Override
    public TreeNode visitQualifiedName(QualifiedName treeNode, C context)
    {
        return new QualifiedName(
                treeNode.getParts());
    }

    @Override
    public TreeNode visitSelect(Select treeNode, C context)
    {
        return new Select(
                treeNode.getItems().stream().map(i -> (SelectItem) i.accept(this, context)).collect(toImmutableList()),
                treeNode.getRelations().stream().map(r -> (AliasedRelation) r.accept(this, context)).collect(toImmutableList()),
                treeNode.getWhere().map(w -> (Expression) w.accept(this, context)));
    }

    @Override
    public TreeNode visitStringLiteral(StringLiteral treeNode, C context)
    {
        return new StringLiteral(
                treeNode.getValue());
    }

    @Override
    public TreeNode visitSubqueryRelation(SubqueryRelation treeNode, C context)
    {
        return new SubqueryRelation(
                (Select) treeNode.getSelect().accept(this, context));
    }

    @Override
    public TreeNode visitTableName(TableName treeNode, C context)
    {
        return new TableName(
                treeNode.getQualifiedName());
    }
}
