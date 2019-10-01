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
package com.wrmsr.tokamak.core.parse.node.visitor;

import com.wrmsr.tokamak.core.parse.node.TAliasedRelation;
import com.wrmsr.tokamak.core.parse.node.TAllSelectItem;
import com.wrmsr.tokamak.core.parse.node.TExpression;
import com.wrmsr.tokamak.core.parse.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.parse.node.TIdentifier;
import com.wrmsr.tokamak.core.parse.node.TNullLiteral;
import com.wrmsr.tokamak.core.parse.node.TNumberLiteral;
import com.wrmsr.tokamak.core.parse.node.TQualifiedName;
import com.wrmsr.tokamak.core.parse.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.node.TRelation;
import com.wrmsr.tokamak.core.parse.node.TSelect;
import com.wrmsr.tokamak.core.parse.node.TSelectItem;
import com.wrmsr.tokamak.core.parse.node.TStringLiteral;
import com.wrmsr.tokamak.core.parse.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.parse.node.TTableName;
import com.wrmsr.tokamak.core.parse.node.TNode;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class TNodeRewriter<C>
        extends TNodeVisitor<TNode, C>
{
    @Override
    protected TNode visitTreeNode(TNode treeNode, C context)
    {
        throw new IllegalStateException();
    }

    @Override
    public TNode visitAliasedRelation(TAliasedRelation treeNode, C context)
    {
        return new TAliasedRelation(
                (TRelation) treeNode.getRelation().accept(this, context),
                treeNode.getAlias());
    }

    @Override
    public TNode visitAllSelectItem(TAllSelectItem treeNode, C context)
    {
        return new TAllSelectItem();
    }

    @Override
    public TNode visitExpressionSelectItem(TExpressionSelectItem treeNode, C context)
    {
        return new TExpressionSelectItem(
                (TExpression) treeNode.getExpression().accept(this, context),
                treeNode.getLabel());
    }

    @Override
    public TNode visitFunctionCallExpression(TFunctionCallExpression treeNode, C context)
    {
        return new TFunctionCallExpression(
                treeNode.getName(),
                treeNode.getArgs().stream().map(a -> (TExpression) a.accept(this, context)).collect(toImmutableList()));
    }

    @Override
    public TNode visitIdentifier(TIdentifier treeNode, C context)
    {
        return new TIdentifier(
                treeNode.getValue());
    }

    @Override
    public TNode visitNullLiteral(TNullLiteral treeNode, C context)
    {
        return new TNullLiteral();
    }

    @Override
    public TNode visitNumberLiteral(TNumberLiteral treeNode, C context)
    {
        return new TNumberLiteral(
                treeNode.getValue());
    }

    @Override
    public TNode visitQualifiedName(TQualifiedName treeNode, C context)
    {
        return new TQualifiedName(
                treeNode.getParts());
    }

    @Override
    public TNode visitQualifiedNameExpression(TQualifiedNameExpression treeNode, C context)
    {
        return new TQualifiedNameExpression(
                (TQualifiedName) treeNode.getQualifiedName().accept(this, context));
    }

    @Override
    public TNode visitSelect(TSelect treeNode, C context)
    {
        return new TSelect(
                treeNode.getItems().stream().map(i -> (TSelectItem) i.accept(this, context)).collect(toImmutableList()),
                treeNode.getRelations().stream().map(r -> (TAliasedRelation) r.accept(this, context)).collect(toImmutableList()),
                treeNode.getWhere().map(w -> (TExpression) w.accept(this, context)));
    }

    @Override
    public TNode visitStringLiteral(TStringLiteral treeNode, C context)
    {
        return new TStringLiteral(
                treeNode.getValue());
    }

    @Override
    public TNode visitSubqueryRelation(TSubqueryRelation treeNode, C context)
    {
        return new TSubqueryRelation(
                (TSelect) treeNode.getSelect().accept(this, context));
    }

    @Override
    public TNode visitTableName(TTableName treeNode, C context)
    {
        return new TTableName(
                (TQualifiedName) treeNode.getQualifiedName().accept(this, context));
    }
}
