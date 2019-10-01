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
package com.wrmsr.tokamak.core.driver.context.diag;

import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.HashMap;
import java.util.Map;

public class StatTracker
        implements Stat.Updater
{
    private final Map<PNode, Map<Stat, Object>> valuesByStatByNode = new HashMap<>();

    @Override
    public void update(PNode node, Stat stat, long num)
    {
        valuesByStatByNode.computeIfAbsent(node, n -> new HashMap<>()).compute(stat, (k, v) -> v == null ? 1L : ((Long) v) + num);
    }

    @Override
    public void update(PNode node, Stat stat, double num)
    {
        valuesByStatByNode.computeIfAbsent(node, n -> new HashMap<>()).compute(stat, (k, v) -> v == null ? 1. : ((Double) v) + num);
    }
}
