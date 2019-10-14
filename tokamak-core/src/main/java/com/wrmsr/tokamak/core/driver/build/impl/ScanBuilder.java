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
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ScanBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Serdes;
import com.wrmsr.tokamak.core.serde.impl.TupleSerde;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class ScanBuilder
        extends AbstractBuilder<PScan>
{
    private final List<String> orderedIdFields;
    private final Serde<Object[]> idSerde;

    public ScanBuilder(DriverImpl driver, PScan node, Map<PNode, Builder> sources)
    {
        super(driver, node, sources);

        orderedIdFields = node.getFields().getFieldListsByAnnotationCls().get(IdField.class).stream().map(Field::getName).collect(toImmutableList());

        idSerde = new TupleSerde(
                orderedIdFields.stream()
                        .map(node.getFields()::getType)
                        .map(Serdes.VALUE_SERDES_BY_TYPE::get)
                        .collect(toImmutableList()));
    }

    @Override
    protected void innerBuild(DriverContextImpl dctx, Key key, Consumer<BuildOp> opConsumer)
    {
        Scanner scanner = driver.getScannersByNode().get(node);

        opConsumer.accept(new ScanBuildOp(this, scanner, key, scanRows -> {
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

            opConsumer.accept(new ResponseBuildOp(this, key, rows.build()));
        }));
    }
}
