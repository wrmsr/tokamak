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
package com.wrmsr.tokamak.core.driver.state;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.util.NoExceptAutoCloseable;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public interface StateStorage
{
    interface Context
            extends NoExceptAutoCloseable
    {
    }

    Context createContext();

    void setup()
            throws IOException;

    enum GetFlag
    {
        CREATE,
        LINKAGE,
        ATTRIBUTES,
        SHARE,
        NOLOCK,
    }

    Map<PState, Map<Id, StorageState>> get(Context ctx, Map<PState, Set<Id>> idSetsByNode, EnumSet<GetFlag> flags)
            throws IOException;

    void put(Context ctx, List<StorageState> states, boolean create)
            throws IOException;

    void allocate(Context ctx, PState node, Iterable<Id> ids)
            throws IOException;

    List<Id> getSpanIds(Context ctx, PState node, Span<Id> span, OptionalInt limit)
            throws IOException;
}
