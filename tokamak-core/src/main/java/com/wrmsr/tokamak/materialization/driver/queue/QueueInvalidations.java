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
package com.wrmsr.tokamak.materialization.driver.queue;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.api.NodeId;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class QueueInvalidations
{
    private final Map<NodeId, Set<Id>> invalidations;

    public QueueInvalidations(Map<NodeId, Set<Id>> invalidations)
    {
        this.invalidations = invalidations.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
    }

    public Map<NodeId, Set<Id>> getInvalidations()
    {
        return invalidations;
    }
}
