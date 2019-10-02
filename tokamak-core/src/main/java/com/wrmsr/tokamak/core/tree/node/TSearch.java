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
package com.wrmsr.tokamak.core.tree.node;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TSearch
        extends TExpression
{
    private final SNode search;
    private final List<TExpression> args;

    public TSearch(SNode search, List<TExpression> args)
    {
        this.search = checkNotNull(search);
        this.args = ImmutableList.copyOf(args);
    }

    public SNode getSearch()
    {
        return search;
    }

    public List<TExpression> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitSearch(this, context);
    }
}
