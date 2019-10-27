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
package com.wrmsr.tokamak.core.search.node.visitor;

import com.wrmsr.tokamak.core.search.node.SNode;

import java.util.HashMap;
import java.util.Map;

import static com.wrmsr.tokamak.util.MoreCollections.safeComputeIfAbsent;

public abstract class CachingSNodeVisitor<R, C>
        extends SNodeVisitor<R, C>
{
    protected final Map<SNode, R> cache;

    public CachingSNodeVisitor()
    {
        cache = new HashMap<>();
    }

    public CachingSNodeVisitor(Map<SNode, R> cache)
    {
        this.cache = cache;
    }

    @Override
    public R process(SNode node, C context)
    {
        return safeComputeIfAbsent(cache, node, n -> super.process(node, context));
    }

    public Map<SNode, R> getCache()
    {
        return cache;
    }
}
