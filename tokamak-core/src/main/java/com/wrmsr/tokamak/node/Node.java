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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.api.FieldName;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.api.NodeName;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import java.util.Map;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrossJoinNode.class, name = "cross_join"),
        @JsonSubTypes.Type(value = EquijoinNode.class, name = "equijoin"),
        @JsonSubTypes.Type(value = FilterNode.class, name = "filter"),
        @JsonSubTypes.Type(value = ListAggregateNode.class, name = "list_aggregate"),
        @JsonSubTypes.Type(value = LookupJoinNode.class, name = "lookup_join"),
        @JsonSubTypes.Type(value = PersistNode.class, name = "persist"),
        @JsonSubTypes.Type(value = ProjectNode.class, name = "project"),
        @JsonSubTypes.Type(value = ScanNode.class, name = "scan"),
        @JsonSubTypes.Type(value = UnionNode.class, name = "union"),
        @JsonSubTypes.Type(value = UnnestNode.class, name = "unnest"),
        @JsonSubTypes.Type(value = ValuesNode.class, name = "values"),
})
public interface Node
{
    NodeName getName();

    NodeId getNodeId();

    Set<Node> getChildren();

    Map<FieldName, Type> getFields();

    Set<FieldName> getIdFields();

    <R, C> R accept(NodeVisitor<R, C> visitor, C context);
}
