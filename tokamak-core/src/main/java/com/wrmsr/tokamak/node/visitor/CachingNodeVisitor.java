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
package com.wrmsr.tokamak.node.visitor;

import com.wrmsr.tokamak.node.Node;

import java.util.HashMap;
import java.util.Map;

public abstract class CachingNodeVisitor<R, C>
        extends NodeVisitor<R, C>
{
    protected final Map<Node, R> cache;

    public CachingNodeVisitor()
    {
        cache = new HashMap<>();
    }

    public CachingNodeVisitor(Map<Node, R> cache)
    {
        this.cache = cache;
    }

    protected R get(Node node, C context)
    {
        return cache.computeIfAbsent(node, n -> n.accept(this, context));
    }

    public Map<Node, R> getCache()
    {
        return cache;
    }
}
