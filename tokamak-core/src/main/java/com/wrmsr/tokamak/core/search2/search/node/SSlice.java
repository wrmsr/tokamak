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
package com.wrmsr.tokamak.core.search2.search.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.core.search2.search.node.visitor.SNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SSlice
        extends SAbstractNode
        implements SSingleSource
{
    private final SNode source;
    private final OptionalInt start;
    private final OptionalInt stop;
    private final OptionalInt step;

    @JsonCreator
    public SSlice(
            @JsonProperty("source") SNode source,
            @JsonProperty("start") OptionalInt start,
            @JsonProperty("stop") OptionalInt stop,
            @JsonProperty("step") OptionalInt step)
    {
        this.source = checkNotNull(source);
        this.start = checkNotNull(start);
        this.stop = checkNotNull(stop);
        this.step = checkNotNull(step);
    }

    @JsonProperty("source")
    @Override
    public SNode getSource()
    {
        return source;
    }

    @JsonProperty("start")
    public OptionalInt getStart()
    {
        return start;
    }

    @JsonProperty("stop")
    public OptionalInt getStop()
    {
        return stop;
    }

    @JsonProperty("step")
    public OptionalInt getStep()
    {
        return step;
    }

    @Override
    public <R, C> R accept(SNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitSlice(this, context);
    }
}
