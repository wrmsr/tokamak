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

package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class LockOverride
{
    private final String node;
    private final String field;

    @JsonCreator
    public LockOverride(
            @JsonProperty("node") String node,
            @JsonProperty("field") String field)
    {
        this.node = node;
        this.field = field;
    }

    @Override
    public String toString()
    {
        return "LockOverride{" +
                "node='" + node + '\'' +
                ", field='" + field + '\'' +
                '}';
    }

    @JsonProperty("node")
    public String getNode()
    {
        return node;
    }

    @JsonProperty("field")
    public String getField()
    {
        return field;
    }
}
