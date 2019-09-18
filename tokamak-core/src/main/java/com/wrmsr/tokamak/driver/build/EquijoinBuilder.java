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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.serde.value.NullableValueSerde;
import com.wrmsr.tokamak.serde.value.TupleValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerdes;
import com.wrmsr.tokamak.serde.value.VariableLengthValueSerde;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class EquijoinBuilder
        extends Builder<EquijoinNode>
{
    // FIXME: not necessarily variable
    private static final ValueSerde<byte[]> NULLABLE_BYTES_VALUE_SERDE =
            new NullableValueSerde<>(
                    new VariableLengthValueSerde<>(
                            ValueSerdes.BYTES_VALUE_SERDE));

    public EquijoinBuilder(DriverImpl driver, EquijoinNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        checkArgument(context.getDriver() == driver);

        List<Pair<EquijoinNode.Branch, Map<String, Object>>> lookups;

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
                Map<EquijoinNode.Branch, List<Map.Entry<String, Object>>> m = fieldKey.getValuesByField().entrySet().stream()
                        .collect(Collectors.groupingBy(e -> checkNotNull(node.getBranchesByUniqueField().get(e.getKey()))));
                lookups = m.entrySet().stream()
                        .map(e -> Pair.immutable(e.getKey(), (Map<String, Object>) e.getValue().stream().collect(toImmutableMap())))
                        .collect(toImmutableList());
            }
        }
        else {
            throw new IllegalStateException();
        }

        ImmutableList.Builder<DriverRow> builder = ImmutableList.builder();

        buildLookups(
                context,
                lookups,
                builder,
                new byte[node.getBranches().size()][],
                ImmutableMap.of(),
                context.getDriver().getLineagePolicy().build(),
                0);

        return builder.build();
    }

    protected void buildLookups(
            DriverContextImpl context,
            List<Pair<EquijoinNode.Branch, Map<String, Object>>> lookups,
            ImmutableList.Builder<DriverRow> builder,
            byte[][] idProto,
            Map<String, Object> proto,
            Set<DriverRow> lineage,
            int pos)
    {
        if (pos < lookups.size()) {
            Pair<EquijoinNode.Branch, Map<String, Object>> lookup = lookups.get(pos);

            ImmutableMap.Builder<String, Object> keyBuilder = ImmutableMap.<String, Object>builder()
                    .putAll(lookup.second());
            if (pos > 0) {
                List<String> firstLookupKeyFields = lookups.get(0).getFirst().getFields();
                for (int i = 0; i < node.getKeyLength(); ++i) {
                    keyBuilder.put(lookup.first().getFields().get(i), proto.get(firstLookupKeyFields.get(i)));
                }
            }
            Key key = Key.of(keyBuilder.build());

            int branchIdx = node.getIndicesByBranch().get(lookup.first());

            Collection<DriverRow> rows = context.build(lookup.first().getNode(), key);

            for (DriverRow row : rows) {
                ImmutableMap.Builder<String, Object> nextProto = ImmutableMap.<String, Object>builder()
                        .putAll(proto);
                for (Map.Entry<String, Object> e : row.getMap().entrySet()) {
                    if (proto.containsKey(e.getKey())) {
                        checkState(proto.get(e.getKey()).equals(e.getValue()));
                    }
                    else {
                        nextProto.put(e);
                    }
                }

                byte[][] nextIdProto = idProto.clone();
                nextIdProto[branchIdx] = row.getId().getValue();

                buildLookups(
                        context,
                        lookups,
                        builder,
                        nextIdProto,
                        nextProto.build(),
                        context.getDriver().getLineagePolicy().build(ImmutableSet.<DriverRow>builder().addAll(lineage).add(row).build()),
                        pos + 1);
            }
        }
        else {
            Set<EquijoinNode.Branch> lookupBranches = lookups.stream().map(Pair::first).collect(toImmutableSet());
            List<EquijoinNode.Branch> restBranches = node.getBranches().stream()
                    .filter(b -> !lookupBranches.contains(b))
                    .collect(toImmutableList());

            List<Object> restKeyValues = lookups.get(0).first().getFields().stream()
                    .map(proto::get)
                    .collect(toImmutableList());

            buildNonLookups(
                    context,
                    restBranches,
                    builder,
                    idProto,
                    proto,
                    restKeyValues.toArray(),
                    lineage,
                    0);
        }
    }

    protected void buildNonLookups(
            DriverContextImpl context,
            List<EquijoinNode.Branch> branches,
            ImmutableList.Builder<DriverRow> builder,
            byte[][] idProto,
            Map<String, Object> proto,
            Object[] keyValues,
            Set<DriverRow> lineage,
            int pos)
    {
        if (pos < branches.size()) {
            EquijoinNode.Branch branch = branches.get(pos);

            ImmutableMap.Builder<String, Object> keyBuilder = ImmutableMap.builder();
            for (int i = 0; i < node.getKeyLength(); ++i) {
                keyBuilder.put(branch.getFields().get(i), keyValues[i]);
            }
            Key key = Key.of(keyBuilder.build());

            int branchIdx = node.getIndicesByBranch().get(branch);

            Collection<DriverRow> rows = context.build(branch.getNode(), key);

            for (DriverRow row : rows) {
                ImmutableMap.Builder<String, Object> nextProto = ImmutableMap.<String, Object>builder()
                        .putAll(proto);
                for (Map.Entry<String, Object> e : row.getMap().entrySet()) {
                    if (proto.containsKey(e.getKey())) {
                        checkState(proto.get(e.getKey()).equals(e.getValue()));
                    }
                    else {
                        nextProto.put(e);
                    }
                }

                byte[][] nextIdProto = idProto.clone();
                nextIdProto[branchIdx] = row.getId().getValue();

                buildNonLookups(
                        context,
                        branches,
                        builder,
                        nextIdProto,
                        nextProto.build(),
                        keyValues,
                        context.getDriver().getLineagePolicy().build(ImmutableSet.<DriverRow>builder().addAll(lineage).add(row).build()),
                        pos + 1);
            }
        }
        else {
            Object[] attributes = new Object[node.getRowLayout().getFields().size()];
            for (Map.Entry<String, Object> e : proto.entrySet()) {
                attributes[node.getRowLayout().getPositionsByField().get(e.getKey())] = e.getValue();
            }

            ValueSerde<Object[]> idSerde = new TupleValueSerde(
                    node.getBranches().stream().map(b -> NULLABLE_BYTES_VALUE_SERDE).collect(toImmutableList()));
            Object[] idBytesObjects = new Object[node.getBranches().size()];
            System.arraycopy(idProto, 0, idBytesObjects, 0, idProto.length);
            Id id = new Id(idSerde.writeBytes(idBytesObjects));

            builder.add(
                    new DriverRow(
                            node,
                            lineage,
                            id,
                            attributes));
        }
    }
}
