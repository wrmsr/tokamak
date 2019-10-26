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

import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.analysis.IdAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.OriginAnalysis;
import com.wrmsr.tokamak.core.plan.node.PInvalidatable;
import com.wrmsr.tokamak.core.plan.node.PInvalidating;
import com.wrmsr.tokamak.core.plan.node.PLeaf;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class SetInvalidationsTransform
{
    private SetInvalidationsTransform()
    {
    }

    public static Plan setInvalidations(Plan plan)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan);

        Map<PInvalidating, Map<String, Map<PInvalidatable, Set<String>>>> invalidationMap = new HashMap<>();

        plan.getNodeTypeList(PInvalidatable.class).forEach(invalidatable -> {
            for (IdAnalysis.Part part : idAnalysis.get(invalidatable).getParts()) {
                for (String field : part) {
                    PNodeField nodeField = PNodeField.of(invalidatable, field);
                    Set<OriginAnalysis.Origination> originations = originAnalysis.getStateChainAnalysis()
                            .getFirstOriginationSetsBySink().get(nodeField);
                    checkNotNull(originations);
                    for (OriginAnalysis.Origination origination : originations) {
                        if (origination.getSource().isPresent()) {
                            PNodeField sourceNodeField = origination.getSource().get();
                            Set<OriginAnalysis.Origination> sourceOriginations = originAnalysis.getStateChainAnalysis()
                                    .getFirstOriginationSetsBySink().get(sourceNodeField);
                            checkNotNull(sourceOriginations);
                            for (OriginAnalysis.Origination sourceOrigination : sourceOriginations) {
                                checkState(sourceOrigination.getSink().getNode() instanceof PInvalidating);
                                invalidationMap
                                        .computeIfAbsent((PInvalidating) sourceOrigination.getSink().getNode(), o -> new HashMap<>())
                                        .computeIfAbsent(sourceOrigination.getSink().getField(), o -> new HashMap<>())
                                        .computeIfAbsent(invalidatable, o -> new HashSet<>())
                                        .add(origination.getSink().getField());
                            }
                        }
                        else {
                            checkState(origination.getSink().getNode() instanceof PLeaf);
                        }
                    }
                }
            }
        });

        return new Plan(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitState(PState node, Void context)
            {
                return super.visitState(node, context);
            }
        }, null));
    }
}
