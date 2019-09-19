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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.plan.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class CrossJoinNode
        extends AbstractNode
        implements JoinNode
{
    public enum Mode
    {
        INNER,
        FULL
    }

    private final List<Node> sources;
    private final Mode mode;

    private final Map<String, Type> fields;
    private final Map<String, Node> sourcesByField;

    @JsonCreator
    public CrossJoinNode(
            @JsonProperty("name") String name,
            @JsonProperty("sources") List<Node> sources,
            @JsonProperty("mode") Mode mode)
    {
        super(name);

        this.sources = checkNotEmpty(ImmutableList.copyOf(sources));
        this.mode = checkNotNull(mode);

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();
        ImmutableMap.Builder<String, Node> sourcesByField = ImmutableMap.builder();
        for (Node source : this.sources) {
            for (Map.Entry<String, Type> e : source.getFields().entrySet()) {
                fields.put(e.getKey(), e.getValue());
                sourcesByField.put(e.getKey(), source);
            }
        }
        this.fields = fields.build();
        this.sourcesByField = sourcesByField.build();

        checkInvariants();
    }

    @JsonProperty("sources")
    @Override
    public List<Node> getSources()
    {
        return sources;
    }

    @JsonProperty("mode")
    public Mode getMode()
    {
        return mode;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    public Map<String, Node> getSourcesByField()
    {
        return sourcesByField;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCrossJoinNode(this, context);
    }
}
