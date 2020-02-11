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
package com.wrmsr.tokamak.core.tree;

import com.google.common.base.Joiner;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TBooleanExpression;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TIdentifier;
import com.wrmsr.tokamak.core.tree.node.TJoinRelation;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNotExpression;
import com.wrmsr.tokamak.core.tree.node.TNullLiteral;
import com.wrmsr.tokamak.core.tree.node.TNumberLiteral;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSearch;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableNameRelation;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;

import java.util.function.Consumer;

public final class TreeRendering
{
    private TreeRendering()
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
            public Void process(TNode node, Void context)
            {
                if (node instanceof TExpression) {
                    sb.append("(");
                }
                node.accept(this, context);
                if (node instanceof TExpression) {
                    sb.append(")");
                }
                return null;
            }

            @Override
            public Void visitAliasedRelation(TAliasedRelation treeNode, Void context)
            {
                process(treeNode.getRelation(), context);
                sb.append(" as ").append(treeNode.getAlias());
                return null;
            }

            @Override
            public Void visitAllSelectItem(TAllSelectItem treeNode, Void context)
            {
                sb.append("*");
                return null;
            }

            @Override
            public Void visitBooleanExpression(TBooleanExpression node, Void context)
            {
                process(node.getLeft(), context);
                sb.append(" ");
                sb.append(node.getOp().getString());
                sb.append(" ");
                process(node.getRight(), context);
                return null;
            }

            @Override
            public Void visitComparisonExpression(TComparisonExpression node, Void context)
            {
                process(node.getLeft(), context);
                sb.append(" ");
                sb.append(node.getOp().getString());
                sb.append(" ");
                process(node.getRight(), context);
                return null;
            }

            @Override
            public Void visitExpressionSelectItem(TExpressionSelectItem treeNode, Void context)
            {
                process(treeNode.getExpression(), context);
                treeNode.getLabel().ifPresent(l -> sb.append(" as ").append(l));
                return null;
            }

            @Override
            public Void visitFunctionCallExpression(TFunctionCallExpression treeNode, Void context)
            {
                sb.append(treeNode.getName());
                sb.append("(");
                delimitedForEach(treeNode.getArgs(), ", ", a -> process(a, context));
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
            public Void visitJoinRelation(TJoinRelation treeNode, Void context)
            {
                process(treeNode.getLeft(), context);
                sb.append(" join ");
                process(treeNode.getRight(), context);
                treeNode.getCondition().ifPresent(c -> {
                    sb.append(" on ");
                    process(c, context);
                });
                return null;
            }

            @Override
            public Void visitNotExpression(TNotExpression treeNode, Void context)
            {
                sb.append("not ");
                process(treeNode.getExpression(), context);
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
                process(treeNode.getQualifiedName(), context);
                return null;
            }

            @Override
            public Void visitSearch(TSearch node, Void context)
            {
                // TODO: SearchRendering
                throw new IllegalStateException();
            }

            @Override
            public Void visitSelect(TSelect treeNode, Void context)
            {
                sb.append("select ");
                delimitedForEach(treeNode.getItems(), ", ", i -> process(i, context));
                if (!treeNode.getRelations().isEmpty()) {
                    sb.append(" from ");
                    delimitedForEach(treeNode.getRelations(), ", ", r -> process(r, context));
                }
                treeNode.getWhere().ifPresent(w -> {
                    sb.append(" where ");
                    process(w, context);
                });
                return null;
            }

            @Override
            public Void visitStringLiteral(TStringLiteral treeNode, Void context)
            {
                sb.append(treeNode.getValue().quoted());
                return null;
            }

            @Override
            public Void visitSubqueryRelation(TSubqueryRelation treeNode, Void context)
            {
                sb.append("(");
                process(treeNode.getSelect(), context);
                sb.append(")");
                return null;
            }

            @Override
            public Void visitTableNameRelation(TTableNameRelation treeNode, Void context)
            {
                process(treeNode.getQualifiedName(), context);
                return null;
            }
        }, null);

        return sb.toString();
    }
}
