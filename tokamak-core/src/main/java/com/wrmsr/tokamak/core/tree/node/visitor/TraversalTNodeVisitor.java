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
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;

public class TraversalTNodeVisitor<R, C>
        extends TNodeVisitor<R, C>
{
    @Override
    protected R visitNode(TNode node, C context)
    {
        return null;
    }

    @Override
    public R visitAliasedRelation(TAliasedRelation node, C context)
    {
        process(node.getRelation(), context);

        return super.visitAliasedRelation(node, context);
    }

    @Override
    public R visitExpressionSelectItem(TExpressionSelectItem node, C context)
    {
        process(node.getExpression(), context);

        return super.visitExpressionSelectItem(node, context);
    }

    @Override
    public R visitFunctionCallExpression(TFunctionCallExpression node, C context)
    {
        node.getArgs().forEach(a -> process(a, context));

        return super.visitFunctionCallExpression(node, context);
    }

    @Override
    public R visitQualifiedNameExpression(TQualifiedNameExpression node, C context)
    {
        process(node.getQualifiedName(), context);

        return super.visitQualifiedNameExpression(node, context);
    }

    @Override
    public R visitSelect(TSelect node, C context)
    {
        node.getRelations().forEach(r -> process(r, context));
        node.getItems().forEach(i -> process(i, context));
        node.getWhere().ifPresent(w -> process(w, context));

        return super.visitSelect(node, context);
    }

    @Override
    public R visitSubqueryRelation(TSubqueryRelation node, C context)
    {
        process(node.getSelect(), context);

        return super.visitSubqueryRelation(node, context);
    }
}
