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

import java.util.Objects;

public class QRelationVisitor<R, C>
{
    protected R process(QRelation qrelation, C context)
    {
        return qrelation.accept(this, context);
    }

    protected R visitRelation(QRelation qrelation, C context)
    {
        throw new IllegalStateException(Objects.toString(qrelation));
    }

    public R visitAlias(QAlias qrelation, C context)
    {
        return visitRelation(qrelation, context);
    }

    public R visitJoin(QJoin qrelation, C context)
    {
        return visitRelation(qrelation, context);
    }

    public R visitReferenceRelation(QReferenceRelation qrelation, C context)
    {
        return visitRelation(qrelation, context);
    }

    public R visitSubqueryRelation(QSubqueryRelation qrelation, C context)
    {
        return visitRelation(qrelation, context);
    }
}
