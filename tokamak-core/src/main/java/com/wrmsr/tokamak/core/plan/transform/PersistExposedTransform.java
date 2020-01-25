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
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.annotation.ExposedPNode;

import java.util.List;

public final class PersistExposedTransform
{
    private PersistExposedTransform()
    {
    }

    public static Plan persistExposed(Plan plan)
    {
        List<PNode> exposedNodes = plan.getNodeListsByAnnotationType(ExposedPNode.class);
        if (exposedNodes.isEmpty()) {
            return plan;
        }

        /*
        Map<PNode, PNode> newNodes = exposedNodes.stream()
                .map(node -> {
                    if (node instanceof PSingleSource) {
                        PNode source = ((PSingleSource) node).getSource();
                        if (source instanceof PState) {
                            return node;
                        }

                    }
                    else {

                    }
                })
                .collect(toImmutableMap());
        */

        throw new IllegalStateException();
    }
}
