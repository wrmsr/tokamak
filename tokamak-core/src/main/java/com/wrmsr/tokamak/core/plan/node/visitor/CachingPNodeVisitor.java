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
package com.wrmsr.tokamak.core.plan.node.visitor;

import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.HashMap;
import java.util.Map;

public abstract class CachingPNodeVisitor<R, C>
        extends PNodeVisitor<R, C>
{
    protected final Map<PNode, R> cache;

    public CachingPNodeVisitor()
    {
        cache = new HashMap<>();
    }

    public CachingPNodeVisitor(Map<PNode, R> cache)
    {
        this.cache = cache;
    }

    @Override
    public R process(PNode node, C context)
    {
        return cache.computeIfAbsent(node, n -> super.process(node, context));
    }

    public Map<PNode, R> getCache()
    {
        return cache;
    }
}
