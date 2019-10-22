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
package com.wrmsr.tokamak.core.search.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SAnd.class, name = "and"),
        @JsonSubTypes.Type(value = SCompare.class, name = "compare"),
        @JsonSubTypes.Type(value = SCreateArray.class, name = "createArray"),
        @JsonSubTypes.Type(value = SCreateObject.class, name = "createObject"),
        @JsonSubTypes.Type(value = SCurrent.class, name = "current"),
        @JsonSubTypes.Type(value = SExpressionRef.class, name = "expressionRef"),
        @JsonSubTypes.Type(value = SFlattenArray.class, name = "flattenArray"),
        @JsonSubTypes.Type(value = SFlattenObject.class, name = "flattenObject"),
        @JsonSubTypes.Type(value = SIndex.class, name = "index"),
        @JsonSubTypes.Type(value = SJsonLiteral.class, name = "jsonLiteral"),
        @JsonSubTypes.Type(value = SNegate.class, name = "negate"),
        @JsonSubTypes.Type(value = SOr.class, name = "or"),
        @JsonSubTypes.Type(value = SProject.class, name = "project"),
        @JsonSubTypes.Type(value = SProperty.class, name = "property"),
        @JsonSubTypes.Type(value = SSelection.class, name = "selection"),
        @JsonSubTypes.Type(value = SSequence.class, name = "sequence"),
        @JsonSubTypes.Type(value = SSlice.class, name = "slice"),
        @JsonSubTypes.Type(value = SString.class, name = "string"),
        @JsonSubTypes.Type(value = SParameter.class, name = "variable"),
})
public abstract class SNode
{
    public abstract <R, C> R accept(SNodeVisitor<R, C> visitor, C context);
}
