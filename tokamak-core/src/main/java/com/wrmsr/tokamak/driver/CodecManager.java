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

package com.wrmsr.tokamak.driver;

import com.wrmsr.tokamak.codec.IdCodecs;
import com.wrmsr.tokamak.codec.RowIdCodec;
import com.wrmsr.tokamak.codec.ScalarRowIdCodec;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public class CodecManager
{
    private final Map<Node, Map<Set<String>, RowIdCodec>> rowIdCodecsByIdFieldSetsByNode = new HashMap<>();

    public RowIdCodec getRowIdCodec(Node node, Set<String> idFields)
    {
        Map<Set<String>, RowIdCodec> nodeMap = rowIdCodecsByIdFieldSetsByNode.computeIfAbsent(node, n -> new HashMap<>());
        RowIdCodec rowIdCodec = nodeMap.get(idFields);
        if (rowIdCodec != null) {
            return rowIdCodec;
        }

        checkNotEmpty(idFields);
        checkArgument(node.getIdFieldSets().contains(idFields));

        rowIdCodec = node.accept(new NodeVisitor<RowIdCodec, Void>()
        {
            @Override
            public RowIdCodec visitEquijoinNode(EquijoinNode node, Void context)
            {
                return super.visitEquijoinNode(node, context);
            }

            @Override
            public RowIdCodec visitListAggregateNode(ListAggregateNode node, Void context)
            {
                return new ScalarRowIdCodec<>(
                        node.getGroupField(), IdCodecs.CODECS_BY_TYPE.get(node.getFields().get(node.getGroupField())));
            }
        }, null);

        nodeMap.put(idFields, rowIdCodec);
        return rowIdCodec;
    }
}
