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
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.FunctionCallExpression;
import com.wrmsr.tokamak.core.parse.tree.Identifier;
import com.wrmsr.tokamak.core.parse.tree.NullLiteral;
import com.wrmsr.tokamak.core.parse.tree.NumberLiteral;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.StringLiteral;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstVisitor;

import java.util.function.Consumer;

public final class AstRendering
{
    private AstRendering()
    {
    }

    public static String render(TreeNode node)
    {
        StringBuilder sb = new StringBuilder();

        node.accept(new AstVisitor<Void, Void>()
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
            public Void visitAliasedRelation(AliasedRelation treeNode, Void context)
            {
                treeNode.getRelation().accept(this, context);
                treeNode.getAlias().ifPresent(a -> sb.append(" as ").append(a));
                return null;
            }

            @Override
            public Void visitAllSelectItem(AllSelectItem treeNode, Void context)
            {
                sb.append("*");
                return null;
            }

            @Override
            public Void visitExpressionSelectItem(ExpressionSelectItem treeNode, Void context)
            {
                treeNode.getExpression().accept(this, context);
                treeNode.getLabel().ifPresent(l -> sb.append(" as ").append(l));
                return null;
            }

            @Override
            public Void visitFunctionCallExpression(FunctionCallExpression treeNode, Void context)
            {
                treeNode.getName().accept(this, context);
                sb.append("(");
                delimitedForEach(treeNode.getArgs(), ", ", a -> a.accept(this, context));
                sb.append(")");
                return null;
            }

            @Override
            public Void visitIdentifier(Identifier treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitNullLiteral(NullLiteral treeNode, Void context)
            {
                sb.append("null");
                return null;
            }

            @Override
            public Void visitNumberLiteral(NumberLiteral treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitQualifiedName(QualifiedName treeNode, Void context)
            {
                sb.append(Joiner.on(".").join(treeNode.getParts()));
                return null;
            }

            @Override
            public Void visitSelect(Select treeNode, Void context)
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
            public Void visitStringLiteral(StringLiteral treeNode, Void context)
            {
                sb.append(treeNode.getValue());
                return null;
            }

            @Override
            public Void visitSubqueryRelation(SubqueryRelation treeNode, Void context)
            {
                sb.append("(");
                treeNode.getSelect().accept(this, context);
                sb.append(")");
                return null;
            }

            @Override
            public Void visitTableName(TableName treeNode, Void context)
            {
                treeNode.getQualifiedName().accept(this, context);
                return null;
            }
        }, null);

        return sb.toString();
    }
}
