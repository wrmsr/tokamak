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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RowCacheImpl
        implements RowCache
{
    private Map<Node, Set<DriverRow>> rowSetsByNode = new HashMap<>();
    private Map<Node, Map<Key, Set<DriverRow>>> rowSetsByKeyByNode = new HashMap<>();

    @Override
    public Optional<Collection<DriverRow>> get(Node node, Key key)
    {
        Map<Key, Set<DriverRow>> map = rowSetsByKeyByNode.get(node);
        if (map == null) {
            return Optional.empty();
        }
        Set<DriverRow> set = map.get(key);
        if (set == null) {
            return Optional.empty();
        }
        return Optional.of(set);
    }

    @Override
    public void put(Node node, Key key, Collection<DriverRow> rows)
    {

    }

    @Override
    public Collection<DriverRow> get(Node node)
    {
        return null;
    }

    @Override
    public Map<Node, Collection<DriverRow>> get()
    {
        return null;
    }

    @Override
    public DriverRow get(StatefulNode node, Id id)
    {
        return null;
    }
}
