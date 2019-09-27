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
package com.wrmsr.tokamak.core.parse.tree;

import com.wrmsr.tokamak.core.parse.tree.visitor.AstVisitor;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AliasedRelation
        extends TreeNode
{
    private final Relation relation;
    private final Optional<String> alias;

    public AliasedRelation(Relation relation, Optional<String> alias)
    {
        this.relation = checkNotNull(relation);
        this.alias = checkNotNull(alias);
    }

    public Relation getRelation()
    {
        return relation;
    }

    public Optional<String> getAlias()
    {
        return alias;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitAliasedRelation(this, context);
    }
}
