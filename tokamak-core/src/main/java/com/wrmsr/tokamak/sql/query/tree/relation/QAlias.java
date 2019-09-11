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
package com.wrmsr.tokamak.sql.query.tree.relation;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class QAlias
        extends QRelation
{
    private final QRelation child;
    private final String name;

    public QAlias(QRelation child, String name)
    {
        this.child = checkNotNull(child);
        this.name = checkNotEmpty(name);
        checkArgument(!(child instanceof QAlias));
    }

    public QRelation getChild()
    {
        return child;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public <R, C> R accept(QRelationVisitor<R, C> visitor, C context)
    {
        return visitor.visitQAlias(this, context);
    }
}
