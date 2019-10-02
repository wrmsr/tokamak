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
package com.wrmsr.tokamak.util.sql.query.tree.statement;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.sql.query.tree.expression.QExpression;
import com.wrmsr.tokamak.util.sql.query.tree.relation.QRelation;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class QSelect
        extends QStatement
{
    @Immutable
    public static final class Item
    {
        private final QExpression expression;
        private final Optional<String> label;

        public Item(QExpression expression, Optional<String> label)
        {
            this.expression = checkNotNull(expression);
            this.label = checkNotNull(label);
        }

        public QExpression getExpression()
        {
            return expression;
        }

        public Optional<String> getLabel()
        {
            return label;
        }
    }

    private final List<Item> items;
    private final Optional<QRelation> relation;
    private final Optional<QExpression> where;

    public QSelect(List<Item> items, Optional<QRelation> relation, Optional<QExpression> where)
    {
        this.items = ImmutableList.copyOf(items);
        this.relation = checkNotNull(relation);
        this.where = checkNotNull(where);
    }

    public List<Item> getItems()
    {
        return items;
    }

    public Optional<QRelation> getRelation()
    {
        return relation;
    }

    public Optional<QExpression> getWhere()
    {
        return where;
    }

    @Override
    public <R, C> R accept(QStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitSelect(this, context);
    }
}
