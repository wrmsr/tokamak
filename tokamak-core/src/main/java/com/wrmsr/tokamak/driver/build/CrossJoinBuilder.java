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
package com.wrmsr.tokamak.driver.build;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.plan.node.CrossJoinNode;
import com.wrmsr.tokamak.plan.node.Node;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;

public final class CrossJoinBuilder
        extends AbstractBuilder<CrossJoinNode>
{
    public CrossJoinBuilder(DriverImpl driver, CrossJoinNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        List<Pair<Node, Key>> sourceKeyPairs;
        Node lookupSource = key.getValuesByField().keySet().stream()
                .map(f -> checkNotNull(node.getSourcesByField().get(f)))
                .collect(toSingle());
        sourceKeyPairs = ImmutableList.<Pair<Node, Key>>builder()
                .add(Pair.immutable(lookupSource, key))
                .addAll(node.getSources().stream()
                        .filter(s -> s != lookupSource)
                        .map(s -> Pair.<Node, Key>immutable(s, Key.all()))
                        .collect(toImmutableList()))
                .build();

        throw new IllegalStateException();
    }
}
