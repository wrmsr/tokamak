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
package com.wrmsr.tokamak.core.plan.value.visitor;

import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.plan.value.VSpecial;

public class TraversalVNodeVisitor<R, C>
        extends VNodeVisitor<R, C>
{
    protected C traverseContext(VNode node, C context)
    {
        return context;
    }

    @Override
    protected R visitNode(VNode node, C context)
    {
        return null;
    }

    @Override
    public R visitFunction(VFunction node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getArgs().forEach(a -> process(a, traversedContext));

        return super.visitFunction(node, context);
    }

    @Override
    public R visitSpecial(VSpecial node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getArgs().forEach(a -> process(a, traversedContext));
        return super.visitSpecial(node, context);
    }
}
