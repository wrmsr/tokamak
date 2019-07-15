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
package com.wrmsr.tokamak.materialization.driver.context;

import com.wrmsr.tokamak.materialization.api.Key;
import com.wrmsr.tokamak.materialization.api.Payload;
import com.wrmsr.tokamak.materialization.driver.DriverContext;
import com.wrmsr.tokamak.materialization.node.Node;

import java.util.List;

public final class DriverContextImpl
        implements DriverContext
{
    @Override
    public List<Payload> build(Node node, Key key)
    {
        throw new IllegalStateException();
    }

    @Override
    public void commit()
    {
        throw new IllegalStateException();
    }
}
