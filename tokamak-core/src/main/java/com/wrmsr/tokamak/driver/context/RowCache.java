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

import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.List;
import java.util.Map;

public class RowCache
{
    public void set(Node node, Key key, List<DriverRow> rows)
    {
        throw new IllegalStateException();
    }

    public List<DriverRow> get(Node node)
    {
        throw new IllegalStateException();
    }

    public Map<Node, List<DriverRow>> get()
    {
        throw new IllegalStateException();
    }

    public DriverRow get(StatefulNode node, Id id)
    {
        throw new IllegalStateException();
    }

    private DriverRow get(FieldKey fieldKey)
    {
        throw new IllegalStateException();
    }
}
