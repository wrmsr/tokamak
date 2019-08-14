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
import com.google.common.collect.Lists;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.codec.CompositeRowIdCodec;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public class EquijoinBuilder
        extends AbstractBuilder<EquijoinNode>
{
    public EquijoinBuilder(EquijoinNode node)
    {
        super(node);
    }

    @Override
    public Collection<DriverRow> build(DriverContextImpl context, Key key)
    {
        Map<EquijoinNode.Branch, List<Pair<String, Object>>> lookups;

        if (key instanceof IdKey) {
            throw new IllegalStateException();
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Set<EquijoinNode.Branch> idBranches = node.getBranchSetsByKeyFieldSet().get(fieldKey.getValuesByField().keySet());
            if (idBranches != null) {
                EquijoinNode.Branch idBranch = checkNotNull(idBranches.iterator().next());
                // branchFieldKeyPair = Pair.immutable(idBranch, Key.of())
                throw new IllegalStateException();
            }
            else {
                lookups = fieldKey.getValuesByField().entrySet().stream()
                        .map(e -> Pair.immutable(checkNotNull(node.getBranchesByUniqueField().get(e.getKey())), e))
                        .collect(Collectors.groupingBy(Pair::first)).entrySet().stream()
                        .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().map(p -> Pair.immutable(p.second())).collect(toImmutableList())));
            }
        }
        else {
            throw new IllegalStateException();
        }

        Map<EquijoinNode.Branch, List<DriverRow>> lookupRowLists = lookups.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableList.copyOf(
                        context.build(
                                e.getKey().getNode(),
                                Key.of(e.getValue().stream().collect(toImmutableMap()))))));

        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        for (List<Map.Entry<EquijoinNode.Branch, Collection<DriverRow>>> lookupRows : lookupRowLists) {
            List<Object> keyValues = branchFieldKeyPair.first().getFields().stream()
                    .map(lookupRow.getRowView()::get)
                    .collect(toImmutableList());

            ImmutableList.Builder<List<DriverRow>> innerRowLists = ImmutableList.builder();
            innerRowLists.add(ImmutableList.copyOf(lookupRowLists));
            for (EquijoinNode.Branch nonLookupBranch : node.getBranches()) {
                if (nonLookupBranch == branchFieldKeyPair.first()) {
                    continue;
                }
                FieldKey nonLookupKey = Key.of(
                        IntStream.range(0, node.getKeyLength())
                                .boxed()
                                .collect(toImmutableMap(
                                        i -> nonLookupBranch.getFields().get(i),
                                        keyValues::get)));
                innerRowLists.add(ImmutableList.copyOf(context.build(nonLookupBranch.getNode(), nonLookupKey)));
            }

            for (List<DriverRow> product : Lists.cartesianProduct(innerRowLists.build())) {
                Object[] attributes = new Object[node.getRowLayout().getFields().size()];
                for (DriverRow row : product) {
                    for (Map.Entry<String, Object> e : row.getRowView()) {
                        int pos = node.getRowLayout().getPositionsByField().get(e.getKey());
                        attributes[pos] = e.getValue();
                    }
                }
                Id id = Id.of(
                        CompositeRowIdCodec.join(
                                product.stream()
                                        .map(DriverRow::getId)
                                        .map(Id::getValue)
                                        .collect(toImmutableList())));
                ret.add(
                        new DriverRow(
                                node,
                                context.getDriver().getLineagePolicy().build(product),
                                id,
                                attributes));
            }
        }

        return ret.build();
    }
}
