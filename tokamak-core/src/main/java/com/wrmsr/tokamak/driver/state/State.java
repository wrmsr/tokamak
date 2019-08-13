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
package com.wrmsr.tokamak.driver.state;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class State
{
    public enum Mode
    {
        INVALID,
        PHANTOM,
        SHARED,
        EXCLUSIVE,
        MODIFIED
    }

    private final StateContext context;
    private final Id id;
    private Mode mode;
    private long version;
    private Optional<Linkage> linkage = Optional.empty();
    private Optional<DriverRow> row = Optional.empty();

    public State(StateContext context, Id id, Mode mode)
    {
        this.context = checkNotNull(context);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
    }

    public StateContext getContext()
    {
        return context;
    }

    public StatefulNode getNode()
    {
        return context.getNode();
    }

    public Id getId()
    {
        return id;
    }

    public Mode getMode()
    {
        return mode;
    }

    public long getVersion()
    {
        return version;
    }

    public Optional<Linkage> getLinkage()
    {
        return linkage;
    }

    public Optional<DriverRow> getRow()
    {
        return row;
    }
}
