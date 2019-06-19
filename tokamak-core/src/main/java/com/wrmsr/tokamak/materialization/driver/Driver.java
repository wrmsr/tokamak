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
package com.wrmsr.tokamak.materialization.driver;

import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.api.Key;
import com.wrmsr.tokamak.materialization.api.Payload;
import com.wrmsr.tokamak.materialization.driver.context.DriverContext;
import com.wrmsr.tokamak.materialization.node.Node;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Driver
{
    DriverContext createContext(Connection conn)
            throws IOException;

    List<Payload> build(DriverContext context, Node node, Key key)
            throws IOException;

    List<SyncOutput> sync(DriverContext context, Map<Node, Set<Id>> idSetsByNode)
            throws IOException;
}
