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
package com.wrmsr.tokamak.plan.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.plan.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class CacheNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;

    public CacheNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source)
    {
        super(name);

        this.source = checkNotNull(source);
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return null;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCacheNode(this, context);
    }
}
