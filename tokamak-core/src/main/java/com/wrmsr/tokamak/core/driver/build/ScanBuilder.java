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
package com.wrmsr.tokamak.core.driver.build;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Connection;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.catalog.Schema;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.core.serde.impl.TupleSerde;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Serdes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class ScanBuilder
        extends AbstractBuilder<ScanNode>
{
    private final List<String> orderedIdFields;
    private final Serde<Object[]> idSerde;

    public ScanBuilder(DriverImpl driver, ScanNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);

        checkNotEmpty(node.getIdFields());

        orderedIdFields = node.getFields().keySet().stream()
                .filter(node.getIdFields()::contains)
                .collect(toImmutableList());

        idSerde = new TupleSerde(
                orderedIdFields.stream()
                        .map(node.getFields()::get)
                        .map(Serdes.VALUE_SERDES_BY_TYPE::get)
                        .collect(toImmutableList()));
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl dctx, Key key)
    {
        Scanner scanner = driver.getScannersByNode().get(node);
        Schema schema = dctx.getDriver().getCatalog().getSchemasByName().get(node.getSchemaTable().getSchema());
        Connection connection = dctx.getConnection(schema.getConnector());

        List<Map<String, Object>> scanRows = scanner.scan(connection, key);
        // FIXME: scanners can return empty, driver compensates
        checkState(!scanRows.isEmpty());

        ImmutableList.Builder<DriverRow> rows = ImmutableList.builder();
        for (Map<String, Object> scanRow : scanRows) {
            Object[] idAtts = new Object[orderedIdFields.size()];
            for (int i = 0; i < idAtts.length; ++i) {
                idAtts[i] = scanRow.get(orderedIdFields.get(i));
            }

            Id id = Id.of(idSerde.writeBytes(idAtts));

            Object[] attributes = scanRow.values().toArray();
            DriverRow row = new DriverRow(
                    node,
                    dctx.getDriver().getLineagePolicy().build(),
                    id,
                    attributes);

            rows.add(row);
        }

        return rows.build();
    }
}
