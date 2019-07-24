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
package com.wrmsr.tokamak.jdbc.metadata;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.jdbc.TableIdentifier;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class CompositeIndexMetaData
{
    private final List<IndexMetaData> components;

    public CompositeIndexMetaData(List<IndexMetaData> components)
    {
        this.components = ImmutableList.copyOf(components);
        checkArgument(!components.isEmpty());
    }

    public String getIndexName()
    {
        return components.get(0).getIndexName();
    }

    public TableIdentifier getTableIdentifier()
    {
        return components.get(0).getTableIdentifier();
    }

    public List<IndexMetaData> getComponents()
    {
        return components;
    }
}
