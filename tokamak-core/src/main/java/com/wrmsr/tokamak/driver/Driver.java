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
package com.wrmsr.tokamak.driver;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.plan.Plan;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface Driver
{
    Plan getPlan();

    Catalog getCatalog();

    DriverContext createContext()
            throws IOException;

    Collection<Row> build(DriverContext context, Node node, Key key)
            throws IOException;

    Collection<Row> sync(DriverContext context, Map<Node, Set<Id>> idSetsByNode)
            throws IOException;
}
