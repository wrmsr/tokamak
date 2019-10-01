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
package com.wrmsr.tokamak.core.parse.node;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.parse.node.visitor.TNodeVisitor;

import java.util.List;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class TQualifiedName
        extends TNode
{
    private final List<String> parts;

    public TQualifiedName(List<String> parts)
    {
        this.parts = checkNotEmpty(ImmutableList.copyOf(parts));
    }

    @Override
    public String toString()
    {
        return "QualifiedName{" +
                "parts=" + parts +
                '}';
    }

    public List<String> getParts()
    {
        return parts;
    }

    public String getLast()
    {
        return parts.get(parts.size() - 1);
    }

    public SchemaTable toSchemaTable(Optional<String> defaultSchema)
    {
        if (parts.size() == 1) {
            return SchemaTable.of(defaultSchema.get(), parts.get(0));
        }
        else if (parts.size() == 2) {
            return SchemaTable.of(parts.get(0), parts.get(1));
        }
        else {
            throw new IllegalArgumentException(parts.toString());
        }
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitQualifiedName(this, context);
    }
}
