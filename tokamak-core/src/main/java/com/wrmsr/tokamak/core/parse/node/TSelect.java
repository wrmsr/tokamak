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
package com.wrmsr.tokamak.core.parse.node;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.parse.node.visitor.TNodeVisitor;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TSelect
        extends TStatement
{
    private final List<TSelectItem> items;
    private final List<TAliasedRelation> relations;
    private final Optional<TExpression> where;

    public TSelect(List<TSelectItem> items, List<TAliasedRelation> relations, Optional<TExpression> where)
    {
        this.items = ImmutableList.copyOf(items);
        this.relations = checkNotNull(relations);
        this.where = checkNotNull(where);
    }

    public List<TSelectItem> getItems()
    {
        return items;
    }

    public List<TAliasedRelation> getRelations()
    {
        return relations;
    }

    public Optional<TExpression> getWhere()
    {
        return where;
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitSelect(this, context);
    }
}
