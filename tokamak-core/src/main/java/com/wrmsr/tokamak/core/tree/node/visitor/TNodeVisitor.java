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
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNullLiteral;
import com.wrmsr.tokamak.core.tree.node.TNumberLiteral;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TRelation;
import com.wrmsr.tokamak.core.tree.node.TSearch;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TStatement;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;

import java.util.Objects;

public abstract class TNodeVisitor<R, C>
{
    public R process(TNode node, C context)
    {
        return node.accept(this, context);
    }

    protected R visitNode(TNode node, C context)
    {
        throw new IllegalArgumentException(Objects.toString(node));
    }

    public R visitAliasedRelation(TAliasedRelation node, C context)
    {
        return visitNode(node, context);
    }

    public R visitAllSelectItem(TAllSelectItem node, C context)
    {
        return visitSelectItem(node, context);
    }

    public R visitExpression(TExpression node, C context)
    {
        return visitNode(node, context);
    }

    public R visitExpressionSelectItem(TExpressionSelectItem node, C context)
    {
        return visitSelectItem(node, context);
    }

    public R visitFunctionCallExpression(TFunctionCallExpression node, C context)
    {
        return visitExpression(node, context);
    }

    public R visitIdentifier(TIdentifier node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLiteral(TLiteral node, C context)
    {
        return visitExpression(node, context);
    }

    public R visitNullLiteral(TNullLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    public R visitNumberLiteral(TNumberLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    public R visitQualifiedName(TQualifiedName node, C context)
    {
        return visitNode(node, context);
    }

    public R visitQualifiedNameExpression(TQualifiedNameExpression node, C context)
    {
        return visitExpression(node, context);
    }

    public R visitRelation(TRelation node, C context)
    {
        return visitNode(node, context);
    }

    public R visitSearch(TSearch node, C context)
    {
        return visitExpression(node, context);
    }

    public R visitSelect(TSelect node, C context)
    {
        return visitStatement(node, context);
    }

    public R visitSelectItem(TSelectItem node, C context)
    {
        return visitNode(node, context);
    }

    public R visitStatement(TStatement node, C context)
    {
        return visitNode(node, context);
    }

    public R visitStringLiteral(TStringLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    public R visitSubqueryRelation(TSubqueryRelation node, C context)
    {
        return visitRelation(node, context);
    }

    public R visitTableName(TTableName node, C context)
    {
        return visitRelation(node, context);
    }
}
