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
package com.wrmsr.tokamak.core.plan.analysis.origin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class MissingOriginationsException
        extends RuntimeException
{
    private final PNode node;
    private final Set<String> presentFields;

    private final Set<String> missingFields;

    MissingOriginationsException(PNode node, Set<String> presentFields)
    {
        this.node = checkNotNull(node);
        this.presentFields = checkNotEmpty(ImmutableSet.copyOf(presentFields));

        missingFields = checkNotEmpty(ImmutableSet.copyOf(Sets.difference(node.getFields().getNames(), this.presentFields)));
    }

    @Override
    public String toString()
    {
        return "MissingOriginationsException{" +
                "node=" + node +
                ", presentFields=" + presentFields +
                ", missingFields=" + missingFields +
                '}';
    }

    public PNode getNode()
    {
        return node;
    }

    public Set<String> getPresentFields()
    {
        return presentFields;
    }

    public Set<String> getMissingFields()
    {
        return missingFields;
    }
}
