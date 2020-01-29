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

package com.wrmsr.tokamak.core.plan.value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.core.plan.value.visitor.VNodeVisitor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = VArithmeticBinary.class, name = "arithmeticBinary"),
        @JsonSubTypes.Type(value = VArithmeticUnary.class, name = "arithmeticUnary"),
        @JsonSubTypes.Type(value = VArithmeticBinary.class, name = "constant"),
        @JsonSubTypes.Type(value = VConstant.class, name = "constant"),
        @JsonSubTypes.Type(value = VField.class, name = "field"),
        @JsonSubTypes.Type(value = VFunction.class, name = "function"),
        @JsonSubTypes.Type(value = VLogicalBinary.class, name = "logicalBinary"),
        @JsonSubTypes.Type(value = VLogicalNot.class, name = "logicalNot"),
})
public interface VNode
{
    <R, C> R accept(VNodeVisitor<R, C> visitor, C context);
}
