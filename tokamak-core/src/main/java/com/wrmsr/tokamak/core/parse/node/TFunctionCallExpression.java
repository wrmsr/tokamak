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

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class TFunctionCallExpression
        extends TExpression
{
    private final String name;
    private final List<TExpression> args;

    public TFunctionCallExpression(String name, List<TExpression> args)
    {
        this.name = checkNotEmpty(name);
        this.args = ImmutableList.copyOf(args);
    }

    public String getName()
    {
        return name;
    }

    public List<TExpression> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFunctionCallExpression(this, context);
    }
}
