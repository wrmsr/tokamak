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
package com.wrmsr.tokamak.core.driver;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.core.driver.context.lineage.LineageEntry;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

@Immutable
public final class DriverRow
        implements Row
{
    private final PNode node;
    private final Set<LineageEntry> lineage;

    private final @Nullable Id id;
    private final @Nullable Object[] attributes;

    public DriverRow(
            PNode node,
            Set<LineageEntry> lineage,
            @Nullable Id id,
            @Nullable Object[] attributes)
    {
        this.node = node;
        this.lineage = lineage;
        this.id = id;
        this.attributes = attributes;
    }

    @Override
    public String toString()
    {
        return "DriverRow{" +
                "node=" + node +
                ", id=" + id +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    public PNode getNode()
    {
        return node;
    }

    public Set<LineageEntry> getLineage()
    {
        return lineage;
    }

    public Map<String, Object> getMap()
    {
        checkState(!isEmpty());
        return Collections.unmodifiableMap(new ObjectArrayBackedMap<>(node.getRowLayout().getShape(), attributes));
    }

    @Override
    @Nullable
    public Id getId()
    {
        return id;
    }

    @Override
    @Nullable
    public Object[] getAttributes()
    {
        return attributes;
    }
}
