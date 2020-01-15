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
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TIdentifier;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNullLiteral;
import com.wrmsr.tokamak.core.tree.node.TNumberLiteral;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TRelation;
import com.wrmsr.tokamak.core.tree.node.TSearch;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;

import java.util.Map;

import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public class TNodeRewriter<C>
        extends CachingTNodeVisitor<TNode, C>
{
    public TNodeRewriter()
    {
    }

    public TNodeRewriter(Map<TNode, TNode> cache)
    {
        super(cache);
    }

    @Override
    protected TNode visitNode(TNode node, C context)
    {
        throw new IllegalStateException();
    }

    @Override
    public TNode visitAliasedRelation(TAliasedRelation node, C context)
    {
        return new TAliasedRelation(
                (TRelation) process(node.getRelation(), context),
                node.getAlias());
    }

    @Override
    public TNode visitAllSelectItem(TAllSelectItem node, C context)
    {
        return new TAllSelectItem();
    }

    @Override
    public TNode visitComparisonExpression(TComparisonExpression node, C context)
    {
        return new TComparisonExpression(
                (TExpression) process(node.getLeft(), context),
                node.getOp(),
                (TExpression) process(node.getRight(), context));
    }

    @Override
    public TNode visitExpressionSelectItem(TExpressionSelectItem node, C context)
    {
        return new TExpressionSelectItem(
                (TExpression) process(node.getExpression(), context),
                node.getLabel());
    }

    @Override
    public TNode visitFunctionCallExpression(TFunctionCallExpression node, C context)
    {
        return new TFunctionCallExpression(
                node.getName(),
                immutableMapItems(node.getArgs(), a -> (TExpression) process(a, context)));
    }

    @Override
    public TNode visitIdentifier(TIdentifier node, C context)
    {
        return new TIdentifier(
                node.getValue());
    }

    @Override
    public TNode visitNullLiteral(TNullLiteral node, C context)
    {
        return new TNullLiteral();
    }

    @Override
    public TNode visitNumberLiteral(TNumberLiteral node, C context)
    {
        return new TNumberLiteral(
                node.getValue());
    }

    @Override
    public TNode visitQualifiedName(TQualifiedName node, C context)
    {
        return new TQualifiedName(
                node.getParts());
    }

    @Override
    public TNode visitQualifiedNameExpression(TQualifiedNameExpression node, C context)
    {
        return new TQualifiedNameExpression(
                (TQualifiedName) process(node.getQualifiedName(), context));
    }

    @Override
    public TNode visitSearch(TSearch node, C context)
    {
        return new TSearch(
                node.getSearch());
    }

    @Override
    public TNode visitSelect(TSelect node, C context)
    {
        return new TSelect(
                immutableMapItems(node.getItems(), i -> (TSelectItem) process(i, context)),
                immutableMapItems(node.getRelations(), r -> (TAliasedRelation) process(r, context)),
                node.getWhere().map(w -> (TExpression) process(w, context)));
    }

    @Override
    public TNode visitStringLiteral(TStringLiteral node, C context)
    {
        return new TStringLiteral(
                node.getValue());
    }

    @Override
    public TNode visitSubqueryRelation(TSubqueryRelation node, C context)
    {
        return new TSubqueryRelation(
                (TSelect) process(node.getSelect(), context));
    }

    @Override
    public TNode visitTableName(TTableName node, C context)
    {
        return new TTableName(
                (TQualifiedName) process(node.getQualifiedName(), context));
    }
}
