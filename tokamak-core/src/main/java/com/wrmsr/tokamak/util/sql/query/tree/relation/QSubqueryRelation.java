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
package com.wrmsr.tokamak.util.sql.query.tree.relation;

import com.wrmsr.tokamak.util.sql.query.tree.statement.QSelect;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class QSubqueryRelation
        extends QRelation
{
    private final QSelect query;

    public QSubqueryRelation(QSelect query)
    {
        this.query = checkNotNull(query);
    }

    public QSelect getQuery()
    {
        return query;
    }

    @Override
    public <R, C> R accept(QRelationVisitor<R, C> visitor, C context)
    {
        return visitor.visitQSubqueryRelation(this, context);
    }
}
