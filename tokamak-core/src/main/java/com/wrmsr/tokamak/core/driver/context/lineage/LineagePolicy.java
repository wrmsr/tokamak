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
package com.wrmsr.tokamak.core.driver.context.lineage;

import com.wrmsr.tokamak.core.driver.DriverRow;

import java.util.Iterator;
import java.util.Set;

import static com.wrmsr.tokamak.util.MoreCollections.arrayIterate;

public interface LineagePolicy
{
    Set<LineageEntry> build(Iterator<DriverRow> rows);

    default Set<LineageEntry> build(Iterable<DriverRow> rows)
    {
        return build(rows.iterator());
    }

    default Set<LineageEntry> build(DriverRow... rows)
    {
        return build(arrayIterate(rows));
    }
}
