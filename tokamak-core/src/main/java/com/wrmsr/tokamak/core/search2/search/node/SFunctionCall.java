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
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.search2.search.node.visitor.SNodeVisitor;

import java.util.List;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class SFunctionCall
        extends SNode
{
    private final String name;
    private final List<SNode> args;

    @JsonCreator
    public SFunctionCall(
            @JsonProperty("name") String name,
            @JsonProperty("args") List<SNode> args)
    {
        this.name = checkNotEmpty(name);
        this.args = ImmutableList.copyOf(args);
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("args")
    public List<SNode> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(SNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFunctionCall(this, context);
    }
}
