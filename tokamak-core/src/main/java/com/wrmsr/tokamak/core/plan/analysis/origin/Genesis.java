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
package com.wrmsr.tokamak.core.plan.analysis.origin;

public enum Genesis
{
    DIRECT(false),

    INNER_JOIN(false),
    LEFT_JOIN_PRIMARY(false),
    LEFT_JOIN_SECONDARY(false),
    FULL_JOIN(false),
    LOOKUP_JOIN(false),

    SCAN(true),
    VALUES(true),

    GROUP(true),

    OPAQUE(true);

    final boolean leaf;

    Genesis(boolean leaf)
    {
        this.leaf = leaf;
    }

    public boolean isLeaf()
    {
        return leaf;
    }
}
