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

import com.wrmsr.tokamak.codec.row.CompositeRowCodec;
import com.wrmsr.tokamak.codec.row.RowCodec;
import com.wrmsr.tokamak.codec.row.RowCodecs;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

public class CodecManager
{
    private final Map<Node, RowCodec> rowIdCodecsByNode = new HashMap<>();

    public RowCodec getRowIdCodec(Node node)
    {
        RowCodec rowCodec = rowIdCodecsByNode.get(node);
        if (rowCodec != null) {
            return rowCodec;
        }

        rowCodec = node.accept(new NodeVisitor<RowCodec, Void>()
        {
            @Override
            public RowCodec visitEquijoinNode(EquijoinNode node, Void context)
            {
                return new CompositeRowCodec(
                        node.getBranches().stream()
                                .map(EquijoinNode.Branch::getNode)
                                .map(CodecManager.this::getRowIdCodec)
                                .collect(toImmutableList()));
            }

            @Override
            public RowCodec visitFilterNode(FilterNode node, Void context)
            {
                return getRowIdCodec(node.getSource());
            }

            @Override
            public RowCodec visitListAggregateNode(ListAggregateNode node, Void context)
            {
                return RowCodecs.buildRowCodec(node.getGroupField(), node.getFields().get(node.getGroupField()));
            }

            @Override
            public RowCodec visitProjectNode(ProjectNode node, Void context)
            {
                // if (node.getProjection().getInputFieldsByOutput())
                return super.visitProjectNode(node, context);
            }

            @Override
            public RowCodec visitScanNode(ScanNode node, Void context)
            {
                checkNotEmpty(node.getIdFields());
                List<String> orderedFields = node.getFields().keySet().stream()
                        .filter(node.getIdFields()::contains)
                        .collect(toImmutableList());
                return RowCodecs.buildRowCodec(
                        orderedFields.stream()
                                .collect(toImmutableMap(identity(), node.getFields()::get)));
            }
        }, null);

        rowIdCodecsByNode.put(node, rowCodec);
        return rowCodec;
    }

    private final Map<StatefulNode, RowCodec> rowCodecsByStatefulNode = new HashMap<>();

    public RowCodec getRowIdCodec(StatefulNode node)
    {
        RowCodec rowCodec = rowCodecsByStatefulNode.get(node);
        if (rowCodec != null) {
            return rowCodec;
        }

        rowCodec = RowCodecs.buildRowCodec(node.getFields());
        rowIdCodecsByNode.put(node, rowCodec);
        return rowCodec;
    }

}
