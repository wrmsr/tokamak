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
import com.wrmsr.tokamak.core.parse.tree.Literal;
import com.wrmsr.tokamak.core.parse.tree.NullLiteral;
import com.wrmsr.tokamak.core.parse.tree.NumberLiteral;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Relation;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SelectItem;
import com.wrmsr.tokamak.core.parse.tree.Statement;
import com.wrmsr.tokamak.core.parse.tree.StringLiteral;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;

import java.util.Objects;

public abstract class AstVisitor<R, C>
{
    protected R visitTreeNode(TreeNode treeNode, C context)
    {
        throw new IllegalArgumentException(Objects.toString(treeNode));
    }

    public R visitAliasedRelation(AliasedRelation treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitAllSelectItem(AllSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitExpression(Expression treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitExpressionSelectItem(ExpressionSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitFunctionCallExpression(FunctionCallExpression treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitIdentifier(Identifier treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitIntegerLiteral(NumberLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitLiteral(Literal treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitNullLiteral(NullLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitQualifiedName(QualifiedName treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitRelation(Relation treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitSelect(Select treeNode, C context)
    {
        return visitStatement(treeNode, context);
    }

    public R visitSelectItem(SelectItem treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStatement(Statement treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStringLiteral(StringLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitSubqueryRelation(SubqueryRelation treeNode, C context)
    {
        return visitRelation(treeNode, context);
    }

    public R visitTableName(TableName treeNode, C context)
    {
        return visitRelation(treeNode, context);
    }
}
