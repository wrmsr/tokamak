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
package com.wrmsr.tokamak.driver;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.layout.RowView;
import com.wrmsr.tokamak.node.Node;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Set;

@Immutable
public final class DriverRow
        implements Row
{
    private final Node node;
    private final Set<DriverRow> lineage;

    private final @Nullable Id id;
    private final @Nullable Object[] attributes;

    public DriverRow(
            Node node,
            Set<DriverRow> lineage,
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
                "node=" + node.getName() +
                "id=" + id +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    public Node getNode()
    {
        return node;
    }

    public Set<DriverRow> getLineage()
    {
        return lineage;
    }

    public RowView getRowView()
    {
        return new RowView(node.getRowLayout(), attributes);
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
