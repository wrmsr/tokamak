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
package com.wrmsr.tokamak.core.plan.dot;

import com.wrmsr.tokamak.core.plan.node.Node;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
final class NodeRendering<T extends Node>
{
    private final Class<T> cls;

    @Nullable
    private final Color color;

    public NodeRendering(Class<T> cls)
    {
        this.cls = checkNotNull(cls);
        this.color = null;
    }

    public NodeRendering(Class<T> cls, Color color)
    {
        this.cls = checkNotNull(cls);
        this.color = checkNotNull(color);
    }

    public NodeRendering(NodeRendering<T> proto, Color color)
    {
        this.cls = proto.cls;
        this.color = checkNotNull(color);
    }

    public Class<T> getCls()
    {
        return cls;
    }

    @Nullable
    public Color getColor()
    {
        return color;
    }
}
