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
package com.wrmsr.tokamak.driver.context;

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class NopRowCacheImpl
        implements RowCache
{
    @Override
    public Optional<Collection<DriverRow>> get(Node node, Key key)
    {
        return Optional.empty();
    }

    @Override
    public void put(Node node, Key key, Collection<DriverRow> rows)
    {
    }

    @Override
    public Collection<DriverRow> getForNode(Node node)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Node, Collection<DriverRow>> getNodeMap()
    {
        throw new UnsupportedOperationException();
    }
}
