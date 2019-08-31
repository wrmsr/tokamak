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

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.node.ScanNode;

import java.util.Collection;

public final class ScanBuilder
        extends Builder<ScanNode>
{
    public ScanBuilder(DriverImpl driver, ScanNode node)
    {
        super(driver, node);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        return null;
    }
}