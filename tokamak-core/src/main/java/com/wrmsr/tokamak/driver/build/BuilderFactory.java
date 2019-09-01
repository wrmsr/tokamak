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

package com.wrmsr.tokamak.driver.build;

import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.node.CrossJoinNode;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.LookupJoinNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.PersistNode;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.UnionNode;
import com.wrmsr.tokamak.node.UnnestNode;
import com.wrmsr.tokamak.node.ValuesNode;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class BuilderFactory
{
    /*
    TODO:
     - Map<Node, Builder> sources Builder arg
    */

    private final DriverImpl driver;

    private final Map<Node, Builder> buildersByNode = new HashMap<>();

    public BuilderFactory(DriverImpl driver)
    {
        this.driver = checkNotNull(driver);
    }

    public synchronized Builder get(Node node)
    {
        Builder builder = buildersByNode.get(node);
        if (builder != null) {
            return builder;
        }

        builder = node.accept(new NodeVisitor<Builder, Void>()
        {
            @Override
            public Builder visitCrossJoinNode(CrossJoinNode node, Void context)
            {
                return new CrossJoinBuilder(driver, node);
            }

            @Override
            public Builder visitEquijoinNode(EquijoinNode node, Void context)
            {
                return new EquijoinBuilder(driver, node);
            }

            @Override
            public Builder visitFilterNode(FilterNode node, Void context)
            {
                return new FilterBuilder(driver, node, get(node.getSource()));
            }

            @Override
            public Builder visitListAggregateNode(ListAggregateNode node, Void context)
            {
                return new ListAggregateBuilder(driver, node, get(node.getSource()));
            }

            @Override
            public Builder visitLookupJoinNode(LookupJoinNode node, Void context)
            {
                return super.visitLookupJoinNode(node, context);
            }

            @Override
            public Builder visitPersistNode(PersistNode node, Void context)
            {
                return new PersistBuilder(driver, node, get(node.getSource()));
            }

            @Override
            public Builder visitProjectNode(ProjectNode node, Void context)
            {
                return new ProjectBuilder(driver, node, get(node.getSource()));
            }

            @Override
            public Builder visitScanNode(ScanNode node, Void context)
            {
                return new ScanBuilder(driver, node);
            }

            @Override
            public Builder visitUnionNode(UnionNode node, Void context)
            {
                return new UnionBuilder(driver, node);
            }

            @Override
            public Builder visitUnnestNode(UnnestNode node, Void context)
            {
                return new UnnestBuilder(driver, node, get(node.getSource()));
            }

            @Override
            public Builder visitValuesNode(ValuesNode node, Void context)
            {
                return new ValuesBuilder(driver, node);
            }
        }, null);

        buildersByNode.put(node, builder);
        return builder;
    }
}
