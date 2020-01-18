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
package com.wrmsr.tokamak.core.plan.transform;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class MergeScansTransform
{
    private MergeScansTransform()
    {
    }

    public static Plan mergeScans(Plan plan)
    {
        Map<SchemaTable, List<PScan>> scanListsBySchemaTable = plan.getNodeTypeList(PScan.class).stream()
                .collect(Collectors.groupingBy(PScan::getSchemaTable));

        Map<PScan, PScan> newScans = scanListsBySchemaTable.entrySet().stream()
                .flatMap(e -> {
                    SchemaTable schemaTable = e.getKey();
                    List<PScan> scans = checkNotEmpty(e.getValue());

                    if (scans.size() == 1) {
                        PScan scan = checkSingle(scans);
                        return ImmutableList.of(Pair.immutable(scan, scan)).stream();
                    }

                    return scans.stream()
                            .map(scan -> Pair.immutable(scan, scan));
                })
                .collect(toImmutableMap());

        return plan;
    }
}
