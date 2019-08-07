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

import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Immutable
public final class FilterNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Predicate<Row> predicate;
    private final boolean unlinked;

    public FilterNode(
            String name,
            Node source,
            Predicate<Row> predicate,
            boolean unlinked)
    {
        super(name);

        this.source = source;
        this.predicate = predicate;
        this.unlinked = unlinked;

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

    public Predicate<Row> getPredicate()
    {
        return predicate;
    }

    public boolean isUnlinked()
    {
        return unlinked;
    }

    @Override
    public Map<String, Type> getFields()
    {
        throw new IllegalStateException();
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilterNode(this, context);
    }
}
