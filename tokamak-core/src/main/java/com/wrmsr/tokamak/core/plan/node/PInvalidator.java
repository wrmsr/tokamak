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
package com.wrmsr.tokamak.core.plan.node;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public interface PInvalidator
        extends PNode
{
    Map<String, Set<String>> getLinkageMasks();

    List<PInvalidation> getInvalidations();

    static void checkInvariants(PInvalidator node)
    {
        node.getLinkageMasks().forEach((sinkNode, fields) ->
                fields.forEach(f -> checkState(node.getFields().contains(f))));
        node.getInvalidations().forEach(inv ->
                inv.getKeyFieldsBySourceField().keySet().forEach(f -> checkState(node.getFields().contains(f))));
    }
}
