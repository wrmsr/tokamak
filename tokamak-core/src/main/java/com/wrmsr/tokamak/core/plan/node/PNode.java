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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PCache.class, name = "cache"),
        @JsonSubTypes.Type(value = PCrossJoin.class, name = "crossJoin"),
        @JsonSubTypes.Type(value = PEquiJoin.class, name = "equiJoin"),
        @JsonSubTypes.Type(value = PFilter.class, name = "filter"),
        @JsonSubTypes.Type(value = PGroupBy.class, name = "groupBy"),
        @JsonSubTypes.Type(value = PLookupJoin.class, name = "lookupJoin"),
        @JsonSubTypes.Type(value = PProject.class, name = "project"),
        @JsonSubTypes.Type(value = PScan.class, name = "scan"),
        @JsonSubTypes.Type(value = PState.class, name = "persist"),
        @JsonSubTypes.Type(value = PStruct.class, name = "struct"),
        @JsonSubTypes.Type(value = PUnion.class, name = "union"),
        @JsonSubTypes.Type(value = PUnnest.class, name = "unnest"),
        @JsonSubTypes.Type(value = PValues.class, name = "values"),
})
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public interface PNode
{
    String getName();

    PNodeId getId();

    PNodeAnnotations getAnnotations();

    List<PNode> getSources();

    FieldCollection getFields();

    default RowLayout getRowLayout()
    {
        return new RowLayout(getFields());
    }

    <R, C> R accept(PNodeVisitor<R, C> visitor, C context);
}
