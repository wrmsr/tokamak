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
import com.wrmsr.tokamak.core.tree.node.TSearch;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;

public class TraversalTNodeVisitor<R, C>
        extends TNodeVisitor<R, C>
{
    @Override
    protected R visitTreeNode(TNode node, C context)
    {
        return null;
    }

    @Override
    public R visitAliasedRelation(TAliasedRelation node, C context)
    {
        node.getRelation().accept(this, context);

        return null;
    }

    @Override
    public R visitExpressionSelectItem(TExpressionSelectItem node, C context)
    {
        node.getExpression().accept(this, context);

        return null;
    }

    @Override
    public R visitFunctionCallExpression(TFunctionCallExpression node, C context)
    {
        node.getArgs().forEach(a -> a.accept(this, context));

        return null;
    }

    @Override
    public R visitQualifiedNameExpression(TQualifiedNameExpression node, C context)
    {
        node.getQualifiedName().accept(this, context);

        return null;
    }

    @Override
    public R visitSelect(TSelect node, C context)
    {
        node.getRelations().forEach(r -> r.accept(this, context));
        node.getItems().forEach(i -> i.accept(this, context));
        node.getWhere().ifPresent(w -> w.accept(this, context));

        return null;
    }

    @Override
    public R visitSubqueryRelation(TSubqueryRelation node, C context)
    {
        node.getSelect().accept(this, context);

        return null;
    }

    @Override
    public R visitSearch(TSearch node, C context)
    {
        node.getArgs().forEach(a -> a.accept(this, context));

        return null;
    }
}
