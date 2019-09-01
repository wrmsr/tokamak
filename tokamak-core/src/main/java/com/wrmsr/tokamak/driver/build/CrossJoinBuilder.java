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
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.node.CrossJoinNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;

public final class CrossJoinBuilder
        extends Builder<CrossJoinNode>
{
    public CrossJoinBuilder(DriverImpl driver, CrossJoinNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        List<Pair<Node, Key>> sourceKeyPairs;
        if (key instanceof AllKey) {
            sourceKeyPairs = node.getSources().stream()
                    .map(s -> Pair.immutable(s, key))
                    .collect(toImmutableList());
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Node lookupSource = fieldKey.getValuesByField().keySet().stream()
                    .map(f -> checkNotNull(node.getSourcesByField().get(f)))
                    .collect(toSingle());
            sourceKeyPairs = ImmutableList.<Pair<Node, Key>>builder()
                    .add(Pair.immutable(lookupSource, key))
                    .addAll(node.getSources().stream()
                            .filter(s -> s != lookupSource)
                            .map(s -> Pair.<Node, Key>immutable(s, Key.all()))
                            .collect(toImmutableList()))
                    .build();
        }
        else if (key instanceof IdKey) {
            // List<Key> idKeys = CompositeRowCodec.split(((IdKey) key).getId().getValue()).stream()
            //         .map(Id::of)
            //         .map(Key::of)
            //         .collect(toImmutableList());
            // checkState(idKeys.size() == node.getSources().size());
            // sourceKeyPairs = Streams.zip(node.getSources().stream(), idKeys.stream(), Pair::immutable)
            //         .collect(toImmutableList());
        }
        else {
            throw new IllegalArgumentException(Objects.toString(key));
        }

        throw new IllegalStateException();
    }
}
