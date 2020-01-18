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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriters;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
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

        Map<PScan, PNode> newScans = scanListsBySchemaTable.entrySet().stream()
                .flatMap(e -> {
                    SchemaTable schemaTable = e.getKey();
                    List<PScan> scans = checkNotEmpty(e.getValue());

                    if (scans.size() == 1) {
                        PScan scan = checkSingle(scans);
                        checkState(scan.getSchemaTable().equals(schemaTable));
                        return ImmutableList.of(Pair.<PScan, PNode>immutable(scan, scan)).stream();
                    }

                    Map<String, Type> allFields = new LinkedHashMap<>();
                    for (PScan scan : scans) {
                        checkState(scan.getSchemaTable().equals(schemaTable));
                        scan.getFields().getTypesByName().forEach((field, type) -> {
                            // FIXME: normalize (de-sigil) + re-sigil
                            if (allFields.containsKey(field)) {
                                checkState(type.equals(allFields.get(field)));
                            }
                            else {
                                allFields.put(field, type);
                            }
                        });
                    }

                    // FIXME: anns?
                    PScan newScan = new PScan(
                            plan.getNodeNameGenerator().get(schemaTable.toString() + "$merged"),
                            AnnotationCollection.of(),
                            AnnotationCollectionMap.of(),
                            schemaTable,
                            allFields,
                            PInvalidations.empty());

                    return scans.stream()
                            .map(scan -> {
                                if (scan.getFields().getTypesByName().equals(allFields)) {
                                    return Pair.<PScan, PNode>immutable(scan, newScan);
                                }

                                PNode drop = new PProject(
                                        plan.getNodeNameGenerator().get(schemaTable.toString() + "$merged$dropped"),
                                        AnnotationCollection.of(),
                                        AnnotationCollectionMap.of(),
                                        newScan,
                                        PProjection.only(Sets.difference(newScan.getFields().getNames(), scan.getFields().getNames())));
                                return Pair.<PScan, PNode>immutable(scan, drop);
                            });
                })
                .collect(toImmutableMap());

        return Plan.of(PNodeRewriters.rewrite(plan.getRoot(), ImmutableMap.copyOf(newScans)));
    }
}
