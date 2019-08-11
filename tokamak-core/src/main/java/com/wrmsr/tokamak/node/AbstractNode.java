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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.util.lazy.GetterLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;
import static org.weakref.jmx.internal.guava.base.Preconditions.checkState;

@Immutable
public abstract class AbstractNode
        implements Node
{
    private final String name;
    private final NodeId nodeId;

    protected AbstractNode(String name)
    {
        this.name = checkNotEmpty(name);
        this.nodeId = NodeId.of(name);
    }

    protected void checkInvariants()
    {
        checkUnique(getSources());
        checkState(getSources().isEmpty() == (this instanceof GeneratorNode));
        Set<String> fieldNames = getFields().keySet();
        getIdFieldSets().forEach(fs -> fs.forEach(f -> checkState(fieldNames.contains(f))));
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }

    @JsonProperty("name")
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public NodeId getId()
    {
        return nodeId;
    }

    private final GetterLazyValue<RowLayout> rowLayout = new GetterLazyValue<>();

    @Override
    public RowLayout getRowLayout()
    {
        return rowLayout.get(() -> new RowLayout(getFields()));
    }
}
