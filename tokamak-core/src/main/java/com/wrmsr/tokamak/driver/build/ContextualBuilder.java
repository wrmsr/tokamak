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

import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.plan.node.Node;

public interface ContextualBuilder<T extends Node>
        extends Builder<T>
{
    BuilderContext buildContext(DriverContextImpl driverContext);

    // default buildWithContext()
}