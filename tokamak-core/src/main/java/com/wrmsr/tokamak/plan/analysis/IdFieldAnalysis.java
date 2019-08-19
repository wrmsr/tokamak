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

package com.wrmsr.tokamak.plan.analysis;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.visitor.CachingNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Immutable
public final class IdFieldAnalysis
{
    private final Map<Node, Set<String>> fieldSetsByNode;

    private IdFieldAnalysis(Map<Node, Set<String>> fieldSetsByNode)
    {
        this.fieldSetsByNode = ImmutableMap.copyOf(fieldSetsByNode);
    }

    public static IdFieldAnalysis analyze(Node node)
    {
        Map<Node, Set<String>> fieldSetsByNode = new HashMap<>();

        node.accept(new CachingNodeVisitor<Set<String>, Void>(fieldSetsByNode)
        {
            @Override
            public Set<String> visitScanNode(ScanNode node, Void context)
            {
                return node.getIdFields();
            }
        }, null);

        return new IdFieldAnalysis(fieldSetsByNode);
    }
}
