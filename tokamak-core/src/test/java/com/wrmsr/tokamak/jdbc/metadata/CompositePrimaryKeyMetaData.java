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

import java.util.Iterator;
import java.util.List;

public class CompositePrimaryKeyMetaData
        implements Iterable<PrimaryKeyMetaData>
{
    private final List<PrimaryKeyMetaData> components;

    public CompositePrimaryKeyMetaData(List<PrimaryKeyMetaData> components)
    {
        this.components = ImmutableList.copyOf(components);
    }

    public List<PrimaryKeyMetaData> getComponents()
    {
        return components;
    }

    @Override
    public Iterator<PrimaryKeyMetaData> iterator()
    {
        return components.iterator();
    }
}