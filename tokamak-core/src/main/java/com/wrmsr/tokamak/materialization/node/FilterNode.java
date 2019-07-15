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

import com.wrmsr.tokamak.materialization.api.Attributes;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;
import java.util.function.Predicate;

@Immutable
public final class FilterNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Predicate<Attributes> predicate;
    private final boolean unlinked;

    public FilterNode(
            NodeName name,
            Node source,
            Predicate<Attributes> predicate,
            Optional<Boolean> unlinked)
    {
        super(name);

        this.source = source;
        this.predicate = predicate;
        this.unlinked = unlinked.get();

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

    public Predicate<Attributes> getPredicate()
    {
        return predicate;
    }

    public boolean isUnlinked()
    {
        return unlinked;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilterNode(this, context);
    }
}
