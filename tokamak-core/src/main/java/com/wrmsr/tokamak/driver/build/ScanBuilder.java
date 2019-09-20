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
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Schema;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.plan.node.Node;
import com.wrmsr.tokamak.plan.node.ScanNode;
import com.wrmsr.tokamak.serde.ByteArrayInput;
import com.wrmsr.tokamak.serde.row.RowSerde;
import com.wrmsr.tokamak.serde.row.RowSerdes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

public final class ScanBuilder
        extends AbstractBuilder<ScanNode>
        implements ContextualBuilder<ScanNode>
{
    private final RowSerde idSerde;

    public ScanBuilder(DriverImpl driver, ScanNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);

        checkNotEmpty(node.getIdFields());
        List<String> orderedFields = node.getFields().keySet().stream()
                .filter(node.getIdFields()::contains)
                .collect(toImmutableList());
        idSerde = RowSerdes.buildRowSerde(
                orderedFields.stream()
                        .collect(toImmutableMap(identity(), node.getFields()::get)));
    }

    private final class Context
            implements BuilderContext
    {
        private final Map<Id, DriverRow> driverRowsById = new HashMap<>();
    }

    @Override
    public BuilderContext buildContext(DriverContextImpl driverContext)
    {
        return new Context();
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl dctx, Key key)
    {
        Context ctx = dctx.getBuildContext(this);

        Scanner scanner = driver.getScannersByNode().get(node);
        Schema schema = dctx.getDriver().getCatalog().getSchemasByName().get(node.getSchemaTable().getSchema());
        Connection connection = dctx.getConnection(schema.getConnector());

        Key scanKey;
        if (key instanceof IdKey) {
            byte[] buf = ((IdKey) key).getId().getValue();
            Map<String, Object> keyFields = idSerde.decodeMap(new ByteArrayInput(buf));
            scanKey = Key.of(keyFields);
        }
        else {
            scanKey = key;
        }

        List<Map<String, Object>> scanRows = scanner.scan(connection, scanKey);
        // FIXME: scanners can return empty, driver compensates
        checkState(!scanRows.isEmpty());

        ImmutableList.Builder<DriverRow> rows = ImmutableList.builder();
        for (Map<String, Object> scanRow : scanRows) {
            Id id = Id.of(idSerde.encodeBytes(scanRow));

            DriverRow row = ctx.driverRowsById.get(id);
            if (row == null) {
                Object[] attributes = scanRow.values().toArray();
                row = new DriverRow(
                        node,
                        dctx.getDriver().getLineagePolicy().build(),
                        id,
                        attributes);
                ctx.driverRowsById.put(id, row);
            }

            rows.add(row);
        }

        return rows.build();
    }
}
