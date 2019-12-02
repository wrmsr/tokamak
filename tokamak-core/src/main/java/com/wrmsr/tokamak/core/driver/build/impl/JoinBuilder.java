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
package com.wrmsr.tokamak.core.driver.build.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Serdes;
import com.wrmsr.tokamak.core.serde.impl.NullableSerde;
import com.wrmsr.tokamak.core.serde.impl.TupleSerde;
import com.wrmsr.tokamak.core.serde.impl.VariableLengthSerde;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class JoinBuilder
        extends AbstractBuilder<PJoin>
{
    /*
    TODO:
     - rewrite entirely
    */

    // FIXME: not necessarily variable
    private static final Serde<byte[]> NULLABLE_BYTES_VALUE_SERDE =
            new NullableSerde<>(
                    new VariableLengthSerde<>(
                            Serdes.BYTES_VALUE_SERDE));

    public JoinBuilder(DriverImpl driver, PJoin node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        checkArgument(context.getDriver() == driver);

        List<Pair<PJoin.Branch, Map<String, Object>>> lookups;

        // Set<PJoin.Branch> idBranches = node.getBranchSetsByKeyFieldSet().get(key.getValuesByField().keySet());
        // if (idBranches != null) {
        //     // FIXME: lol
        //     PJoin.Branch idBranch = checkNotNull(idBranches.iterator().next());
        //     // branchFieldKeyPair = Pair.immutable(idBranch, Key.of())
        //     throw new IllegalStateException();
        // }
        // else {
        Map<PJoin.Branch, List<Map.Entry<String, Object>>> m = key.getValuesByField().entrySet().stream()
                .collect(Collectors.groupingBy(e -> checkNotNull(node.getBranchesByField().get(e.getKey()))));
        lookups = m.entrySet().stream()
                .map(e -> Pair.immutable(e.getKey(), (Map<String, Object>) e.getValue().stream().collect(toImmutableMap())))
                .collect(toImmutableList());
        // }

        ImmutableList.Builder<DriverRow> builder = ImmutableList.builder();

        buildLookups(
                context,
                lookups,
                builder,
                new byte[node.getBranches().size()][],
                ImmutableMap.of(),
                ImmutableSet.of(),
                0,
                key,
                opConsumer);
    }

    protected void buildLookups(
            DriverContextImpl context,
            List<Pair<PJoin.Branch, Map<String, Object>>> lookups,
            ImmutableList.Builder<DriverRow> builder,
            byte[][] idProto,
            Map<String, Object> proto,
            Set<DriverRow> lineage,
            int pos,
            Key origKey,
            Consumer<BuildOp> opConsumer)
    {
        if (pos < lookups.size()) {
            Pair<PJoin.Branch, Map<String, Object>> lookup = lookups.get(pos);

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

            opConsumer.accept(new RequestBuildOp(this, context.getDriver().getBuildersByNode().get(lookup.first().getNode()), key, rows -> {
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
                            ImmutableSet.<DriverRow>builder().addAll(lineage).add(row).build(),
                            pos + 1,
                            origKey,
                            opConsumer);
                }

                if (pos == (lookups.size() - 1)) {
                    opConsumer.accept(new ResponseBuildOp(this, origKey, builder.build()));
                }
            }));
        }
        else {
            Set<PJoin.Branch> lookupBranches = lookups.stream().map(Pair::first).collect(toImmutableSet());
            List<PJoin.Branch> restBranches = node.getBranches().stream()
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
                    0,
                    origKey,
                    opConsumer);
        }
    }

    protected void buildNonLookups(
            DriverContextImpl context,
            List<PJoin.Branch> branches,
            ImmutableList.Builder<DriverRow> builder,
            byte[][] idProto,
            Map<String, Object> proto,
            Object[] keyValues,
            Set<DriverRow> lineage,
            int pos,
            Key origKey,
            Consumer<BuildOp> opConsumer)
    {
        if (pos < branches.size()) {
            PJoin.Branch branch = branches.get(pos);

            ImmutableMap.Builder<String, Object> keyBuilder = ImmutableMap.builder();
            for (int i = 0; i < node.getKeyLength(); ++i) {
                keyBuilder.put(branch.getFields().get(i), keyValues[i]);
            }
            Key key = Key.of(keyBuilder.build());

            int branchIdx = node.getIndicesByBranch().get(branch);

            opConsumer.accept(new RequestBuildOp(this, context.getDriver().getBuildersByNode().get(branch.getNode()), key, rows -> {
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
                            ImmutableSet.<DriverRow>builder().addAll(lineage).add(row).build(),
                            pos + 1,
                            origKey,
                            opConsumer);
                }

                if (branches.size() == node.getBranches().size() && pos == (branches.size() - 1)) {
                    opConsumer.accept(new ResponseBuildOp(this, origKey, builder.build()));
                }
            }));
        }
        else {
            Object[] attributes = new Object[node.getRowLayout().getFields().size()];
            for (Map.Entry<String, Object> e : proto.entrySet()) {
                attributes[node.getRowLayout().getFields().getPosition(e.getKey())] = e.getValue();
            }

            Serde<Object[]> idSerde = new TupleSerde(
                    node.getBranches().stream().map(b -> NULLABLE_BYTES_VALUE_SERDE).collect(toImmutableList()));
            Object[] idBytesObjects = new Object[node.getBranches().size()];
            System.arraycopy(idProto, 0, idBytesObjects, 0, idProto.length);
            Id id = new Id(idSerde.writeBytes(idBytesObjects));

            builder.add(
                    new DriverRow(
                            node,
                            driver.getLineagePolicy().build(lineage),
                            id,
                            attributes));
        }
    }
}
