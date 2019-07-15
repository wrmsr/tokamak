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

import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.materialization.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Immutable
public final class ValuesNode
        extends AbstractNode
        implements GeneratorNode
{
    public ValuesNode(
            NodeName name,
            List<Object> values,
            Optional<FieldName> indexField,
            Optional<Map<FieldName, Type>> typesByField,
            Optional<Boolean> isStrict)
    {
        super(name);

        checkInvariants();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitValuesNode(this, context);
    }
}
