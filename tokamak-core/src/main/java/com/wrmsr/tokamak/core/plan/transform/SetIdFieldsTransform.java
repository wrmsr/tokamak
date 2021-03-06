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
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.analysis.id.IdAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.id.entry.AnonIdAnalysisEntry;
import com.wrmsr.tokamak.core.plan.analysis.id.entry.IdAnalysisEntry;
import com.wrmsr.tokamak.core.plan.analysis.id.part.FieldIdAnalysisPart;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public final class SetIdFieldsTransform
{
    private SetIdFieldsTransform()
    {
    }

    public static Plan setIdFields(Plan plan, Optional<Catalog> catalog)
    {
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan, catalog);

        return Plan.of(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public AnnotationCollectionMap<String, FieldAnnotation> visitNodeFieldAnnotations(PNode node, AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations, Void context)
            {
                List<String> idFields;
                IdAnalysisEntry entry = checkNotNull(idAnalysis.get(node));
                if (!(entry instanceof AnonIdAnalysisEntry) && entry.stream().allMatch(FieldIdAnalysisPart.class::isInstance)) {
                    idFields = entry.stream().map(FieldIdAnalysisPart.class::cast).map(FieldIdAnalysisPart::getField).collect(toImmutableList());
                }
                else {
                    idFields = ImmutableList.of();
                }
                return AnnotationCollectionMap.mergeOf(
                        fieldAnnotations.dropped(IdField.class),
                        idFields.stream().collect(toImmutableMap(identity(), f -> AnnotationCollection.of(FieldAnnotation.id()))));
            }
        }, null));
    }
}
