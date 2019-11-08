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

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.Map;
import java.util.function.Consumer;

public interface Builder<T extends PNode>
{
    DriverImpl getDriver();

    T getNode();

    Map<PNode, Builder<?>> getSources();

    void build(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer);
}
