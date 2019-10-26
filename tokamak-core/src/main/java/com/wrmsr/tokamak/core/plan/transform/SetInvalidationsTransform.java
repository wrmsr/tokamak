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
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;

public final class SetInvalidationsTransform
{
    private SetInvalidationsTransform()
    {
    }

    public static Plan setInvalidations(Plan plan)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan);

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
