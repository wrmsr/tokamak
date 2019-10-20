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
package com.wrmsr.tokamak.core.driver.build;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.build.impl.SearchBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.CacheBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.JoinBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.FilterBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.GroupBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.LookupJoinBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.ProjectBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.ScanBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.StateBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.UnionBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.UnnestBuilder;
import com.wrmsr.tokamak.core.driver.build.impl.ValuesBuilder;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@SuppressWarnings({"rawtypes"})
public class BuilderFactory
{
    private final DriverImpl driver;

    private final Map<PNode, Builder> buildersByNode = new HashMap<>();

    public BuilderFactory(DriverImpl driver)
    {
        this.driver = checkNotNull(driver);
    }

    @FunctionalInterface
    private interface BuilderConstructor
    {
        Builder create(DriverImpl driver, PNode node, Map<PNode, Builder> sources);
    }

    private final Map<Class<? extends PNode>, BuilderConstructor> BUILDER_CONSTRUCTORS_BY_NODE_TYPE =
            ImmutableMap.<Class<? extends PNode>, BuilderConstructor>builder()
                    .put(PCache.class, (d, n, s) -> new CacheBuilder(d, (PCache) n, s))
                    .put(PJoin.class, (d, n, s) -> new JoinBuilder(d, (PJoin) n, s))
                    .put(PFilter.class, (d, n, s) -> new FilterBuilder(d, (PFilter) n, s))
                    .put(PGroup.class, (d, n, s) -> new GroupBuilder(d, (PGroup) n, s))
                    .put(PLookupJoin.class, (d, n, s) -> new LookupJoinBuilder(d, (PLookupJoin) n, s))
                    .put(PProject.class, (d, n, s) -> new ProjectBuilder(d, (PProject) n, s))
                    .put(PScan.class, (d, n, s) -> new ScanBuilder(d, (PScan) n, s))
                    .put(PState.class, (d, n, s) -> new StateBuilder(d, (PState) n, s))
                    .put(PSearch.class, (d, n, s) -> new SearchBuilder(d, (PSearch) n, s))
                    .put(PUnion.class, (d, n, s) -> new UnionBuilder(d, (PUnion) n, s))
                    .put(PUnnest.class, (d, n, s) -> new UnnestBuilder(d, (PUnnest) n, s))
                    .put(PValues.class, (d, n, s) -> new ValuesBuilder(d, (PValues) n, s))
                    .build();

    public synchronized Builder get(PNode node)
    {
        Builder builder = buildersByNode.get(node);
        if (builder != null) {
            return builder;
        }

        Map<PNode, Builder> sources = node.getSources().stream()
                .collect(toImmutableMap(identity(), this::get));

        BuilderConstructor ctor = checkNotNull(BUILDER_CONSTRUCTORS_BY_NODE_TYPE.get(node.getClass()));
        builder = ctor.create(driver, node, sources);

        buildersByNode.put(node, builder);
        return builder;
    }
}
