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

import com.wrmsr.tokamak.core.plan.node.Node;

public enum Stat
{
    ROW,
    ROW_CACHE_HIT,
    ROW_CACHE_MISS,

    STATE,
    STATE_CACHE_HIT,
    STATE_CACHE_MISS,
    STATE_CACHE_LOAD_TIME,
    STATE_INVALIDATION,
    STATE_FLUSH,
    STATE_PHANTOM_UPGRADE,

    SCAN_QUERY,
    SCAN_ROW,
    SCAN_METADATA_ROW,
    SCAN_LOAD_TIME,

    OUTPUT_WRITE,
    OUTPUT_ROW,

    DENORMALIZED_SRC,
    DENORMALIZED_SRC_ROW,
    DENORMALIZED_DST,
    DENORMALIZED_DST_ROW,

    QUEUE_SPILL,

    ;

    public interface Updater
    {
        default void update(Node node, Stat stat)
        {
            update(node, stat, 1L);
        }

        void update(Node node, Stat stat, long num);

        void update(Node node, Stat stat, double num);

        static Updater nop()
        {
            return new Updater()
            {
                @Override
                public void update(Node node, Stat stat, long num)
                {
                }

                @Override
                public void update(Node node, Stat stat, double num)
                {
                }
            };
        }
    }
}
