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
package com.wrmsr.tokamak.materialization.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeId;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.type.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProjectNode.class, name = "project")
})
public abstract class Node
{
    // private final NodeName name;

    // public Node(NodeName name)
    // {
    //     this.name = name;
    // }

    public NodeName getName()
    {
        throw new IllegalStateException();
    }

    public NodeId getNodeId()
    {
        throw new IllegalStateException();
    }

    public List<Node> getChildren()
    {
        throw new IllegalStateException();
    }

    public Set<FieldName> getIdFields()
    {
        throw new IllegalStateException();
    }

    public Map<FieldName, Type> getTypesByField()
    {
        throw new IllegalStateException();
    }
}
