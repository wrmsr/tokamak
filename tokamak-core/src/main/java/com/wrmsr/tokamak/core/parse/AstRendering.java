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
package com.wrmsr.tokamak.core.parse;

import com.google.common.base.Joiner;
import com.wrmsr.tokamak.core.parse.node.TAliasedRelation;
import com.wrmsr.tokamak.core.parse.node.TAllSelectItem;
import com.wrmsr.tokamak.core.parse.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.parse.node.TIdentifier;
import com.wrmsr.tokamak.core.parse.node.TNullLiteral;
import com.wrmsr.tokamak.core.parse.node.TNumberLiteral;
import com.wrmsr.tokamak.core.parse.node.TQualifiedName;
import com.wrmsr.tokamak.core.parse.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.node.TSelect;
import com.wrmsr.tokamak.core.parse.node.TStringLiteral;
import com.wrmsr.tokamak.core.parse.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.parse.node.TTableName;
import com.wrmsr.tokamak.core.parse.node.TNode;
import com.wrmsr.tokamak.core.parse.node.visitor.TNodeVisitor;

import java.util.function.Consumer;

public final class AstRendering
{
    private AstRendering()
    {
    }

    public static String render(TNode node)
    {
        StringBuilder sb = new StringBuilder();

        node.accept(new TNodeVisitor<Void, Void>()
        {
            private <T> void delimitedForEach(Iterable<T> items, String delimiter, Consumer<T> consumer)
            {
                boolean delimit = false;
                for (T item : items) {
                    if (delimit) {
                        sb.append(delimiter);
                    }
                    else {
                        delimit = true;
                    }
                    consumer.accept(item);
                }
            }

            @Override
            public Void visitAliasedRelation(TAliasedRelation treeNode, Void context)
            {
                treeNode.getRelation().accept(this, context);
                treeNode.getAlias().ifPresent(a -> sb.append(" as ").append(a));
                return null;
            }

            @Override
            public Void visitAllSelectItem(TAllSelectItem treeNode, Void context)
            {
                sb.append("*");
                return null;
            }

            @Override
            public Void visitExpressionSelectItem(TExpressionSelectItem treeNode, Void context)
            {
                treeNode.getExpression().accept(this, context);
                treeNode.getLabel().ifPresent(l -> sb.append(" as ").append(l));
                return null;
            }

            @Override
            public Void visitFunctionCallExpression(TFunctionCallExpression treeNode, Void context)
            {
                sb.append(treeNode.getName());
                sb.append("(");
                delimitedForEach(treeNode.getArgs(), ", ", a -> a.accept(this, context));
                sb.append(")");
                return null;
            }

            @Override
            public Void visitIdentifier(TIdentifier treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitNullLiteral(TNullLiteral treeNode, Void context)
            {
                sb.append("null");
                return null;
            }

            @Override
            public Void visitNumberLiteral(TNumberLiteral treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitQualifiedName(TQualifiedName treeNode, Void context)
            {
                sb.append(Joiner.on(".").join(treeNode.getParts()));
                return null;
            }

            @Override
            public Void visitQualifiedNameExpression(TQualifiedNameExpression treeNode, Void context)
            {
                treeNode.getQualifiedName().accept(this, context);
                return null;
            }

            @Override
            public Void visitSelect(TSelect treeNode, Void context)
            {
                sb.append("select ");
                delimitedForEach(treeNode.getItems(), ", ", i -> i.accept(this, context));
                if (!treeNode.getRelations().isEmpty()) {
                    sb.append(" from ");
                    delimitedForEach(treeNode.getRelations(), ", ", r -> r.accept(this, context));
                }
                treeNode.getWhere().ifPresent(w -> {
                    sb.append(" where ");
                    w.accept(this, context);
                });
                return null;
            }

            @Override
            public Void visitStringLiteral(TStringLiteral treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitSubqueryRelation(TSubqueryRelation treeNode, Void context)
            {
                sb.append("(");
                treeNode.getSelect().accept(this, context);
                sb.append(")");
                return null;
            }

            @Override
            public Void visitTableName(TTableName treeNode, Void context)
            {
                treeNode.getQualifiedName().accept(this, context);
                return null;
            }
        }, null);

        return sb.toString();
    }
}
