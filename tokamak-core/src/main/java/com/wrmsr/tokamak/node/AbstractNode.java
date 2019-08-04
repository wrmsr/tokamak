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

import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.type.Type;

import java.util.Map;
import java.util.Set;

import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;
import static org.weakref.jmx.internal.guava.base.Preconditions.checkNotNull;

public abstract class AbstractNode
        implements Node
{
    private final String name;
    private final NodeId nodeId;

    public AbstractNode(String name)
    {
        this.name = checkNotNull(name);
        this.nodeId = NodeId.of(name);
    }

    protected void checkInvariants()
    {
        checkUnique(getSources());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public NodeId getNodeId()
    {
        return nodeId;
    }

    @Override
    public Map<String, Type> getFields()
    {
        throw new IllegalStateException();
    }

    @Override
    public Set<String> getIdFields()
    {
        throw new IllegalStateException();
    }
}
