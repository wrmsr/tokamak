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
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TIdentifier;
import com.wrmsr.tokamak.core.tree.node.TLiteral;
import com.wrmsr.tokamak.core.tree.node.TNullLiteral;
import com.wrmsr.tokamak.core.tree.node.TNumberLiteral;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TRelation;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TStatement;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import com.wrmsr.tokamak.core.tree.node.TNode;

import java.util.Objects;

public abstract class TNodeVisitor<R, C>
{
    protected R visitTreeNode(TNode treeNode, C context)
    {
        throw new IllegalArgumentException(Objects.toString(treeNode));
    }

    public R visitAliasedRelation(TAliasedRelation treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitAllSelectItem(TAllSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitExpression(TExpression treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitExpressionSelectItem(TExpressionSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitFunctionCallExpression(TFunctionCallExpression treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitIdentifier(TIdentifier treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitLiteral(TLiteral treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitNullLiteral(TNullLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitNumberLiteral(TNumberLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitQualifiedName(TQualifiedName treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitQualifiedNameExpression(TQualifiedNameExpression treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitRelation(TRelation treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitSelect(TSelect treeNode, C context)
    {
        return visitStatement(treeNode, context);
    }

    public R visitSelectItem(TSelectItem treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStatement(TStatement treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStringLiteral(TStringLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitSubqueryRelation(TSubqueryRelation treeNode, C context)
    {
        return visitRelation(treeNode, context);
    }

    public R visitTableName(TTableName treeNode, C context)
    {
        return visitRelation(treeNode, context);
    }
}
