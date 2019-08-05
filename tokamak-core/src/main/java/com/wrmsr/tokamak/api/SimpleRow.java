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
package com.wrmsr.tokamak.api;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.util.Arrays;

@Immutable
public final class SimpleRow
        implements Row
{
    private final @Nullable Id id;
    private final @Nullable Object[] attributes;

    public SimpleRow(
            @Nullable Id id,
            @Nullable Object[] attributes)
    {
        this.id = id;
        this.attributes = attributes;
    }

    @Override
    public String toString()
    {
        return "Row{" +
                "id=" + id +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
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
