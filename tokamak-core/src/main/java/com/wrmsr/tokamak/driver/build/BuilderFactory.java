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

import com.google.common.collect.ImmutableMap;
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

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public class BuilderFactory
{
    private final DriverImpl driver;

    private final Map<Node, Builder> buildersByNode = new HashMap<>();

    public BuilderFactory(DriverImpl driver)
    {
        this.driver = checkNotNull(driver);
    }

    @FunctionalInterface
    private interface BuilderConstructor
    {
        Builder create(DriverImpl driver, Node node, Map<Node, Builder> sources);
    }

    private final Map<Class<? extends Node>, BuilderConstructor> BUILDER_CONSTRUCTORS_BY_NODE_TYPE =
            ImmutableMap.<Class<? extends Node>, BuilderConstructor>builder()
                    .put(CrossJoinNode.class, (d, n, s) -> new CrossJoinBuilder(d, (CrossJoinNode) n, s))
                    .put(EquijoinNode.class, (d, n, s) -> new EquijoinBuilder(d, (EquijoinNode) n, s))
                    .put(FilterNode.class, (d, n, s) -> new FilterBuilder(d, (FilterNode) n, s))
                    .put(ListAggregateNode.class, (d, n, s) -> new ListAggregateBuilder(d, (ListAggregateNode) n, s))
                    .put(LookupJoinNode.class, (d, n, s) -> new LookupJoinBuilder(d, (LookupJoinNode) n, s))
                    .put(PersistNode.class, (d, n, s) -> new PersistBuilder(d, (PersistNode) n, s))
                    .put(ProjectNode.class, (d, n, s) -> new ProjectBuilder(d, (ProjectNode) n, s))
                    .put(ScanNode.class, (d, n, s) -> new ScanBuilder(d, (ScanNode) n, s))
                    .put(UnionNode.class, (d, n, s) -> new UnionBuilder(d, (UnionNode) n, s))
                    .put(UnnestNode.class, (d, n, s) -> new UnnestBuilder(d, (UnnestNode) n, s))
                    .put(ValuesNode.class, (d, n, s) -> new ValuesBuilder(d, (ValuesNode) n, s))
                    .build();

    public synchronized Builder get(Node node)
    {
        Builder builder = buildersByNode.get(node);
        if (builder != null) {
            return builder;
        }

        Map<Node, Builder> sources = node.getSources().stream()
                .collect(toImmutableMap(identity(), this::get));

        BuilderConstructor ctor = checkNotNull(BUILDER_CONSTRUCTORS_BY_NODE_TYPE.get(node.getClass()));
        builder = ctor.create(driver, node, sources);

        buildersByNode.put(node, builder);
        return builder;
    }
}
