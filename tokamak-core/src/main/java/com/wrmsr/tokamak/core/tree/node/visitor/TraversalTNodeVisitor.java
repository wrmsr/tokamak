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
import com.wrmsr.tokamak.core.tree.node.TBooleanExpression;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNotExpression;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;

public class TraversalTNodeVisitor<R, C>
        extends TNodeVisitor<R, C>
{
    protected C traverseContext(TNode node, C context)
    {
        return context;
    }

    @Override
    protected R visitNode(TNode node, C context)
    {
        return null;
    }

    @Override
    public R visitAliasedRelation(TAliasedRelation node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getRelation(), traversedContext);

        return super.visitAliasedRelation(node, context);
    }

    @Override
    public R visitBooleanExpression(TBooleanExpression node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getLeft(), traversedContext);
        process(node.getRight(), traversedContext);

        return super.visitBooleanExpression(node, context);
    }

    @Override
    public R visitComparisonExpression(TComparisonExpression node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getLeft(), traversedContext);
        process(node.getRight(), traversedContext);

        return super.visitComparisonExpression(node, context);
    }

    @Override
    public R visitExpressionSelectItem(TExpressionSelectItem node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getExpression(), traversedContext);

        return super.visitExpressionSelectItem(node, context);
    }

    @Override
    public R visitFunctionCallExpression(TFunctionCallExpression node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getArgs().forEach(a -> process(a, traversedContext));

        return super.visitFunctionCallExpression(node, context);
    }

    @Override
    public R visitNotExpression(TNotExpression node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getExpression(), traversedContext);

        return super.visitNotExpression(node, context);
    }

    @Override
    public R visitQualifiedNameExpression(TQualifiedNameExpression node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getQualifiedName(), traversedContext);

        return super.visitQualifiedNameExpression(node, context);
    }

    @Override
    public R visitSelect(TSelect node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getRelations().forEach(r -> process(r, traversedContext));
        node.getItems().forEach(i -> process(i, traversedContext));
        node.getWhere().ifPresent(w -> process(w, traversedContext));

        return super.visitSelect(node, context);
    }

    @Override
    public R visitSubqueryRelation(TSubqueryRelation node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getSelect(), traversedContext);

        return super.visitSubqueryRelation(node, context);
    }
}
