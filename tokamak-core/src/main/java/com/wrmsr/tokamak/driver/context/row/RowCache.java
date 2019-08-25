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
package com.wrmsr.tokamak.driver.context.row;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.MorePreconditions;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface RowCache
{
    Optional<Collection<DriverRow>> get(Node node, Key key);

    void put(Node node, Key key, Collection<DriverRow> rows);

    Collection<DriverRow> getForNode(Node node);

    Map<Node, Collection<DriverRow>> getNodeMap();

    default Optional<DriverRow> get(StatefulNode node, Id id)
    {
        return get(node, Key.of(id)).map(MorePreconditions::checkSingle);
    }

    class KeyException
            extends RuntimeException
    {
        private final Key key;

        public KeyException(Key key)
        {
            this.key = checkNotNull(key);
        }

        @Override
        public String toString()
        {
            return "RowCache.KeyException{" +
                    "key=" + key +
                    '}';
        }
    }
}
