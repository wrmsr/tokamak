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
package com.wrmsr.tokamak.exec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class Signature
{
    private final Type type;
    private final Map<String, Type> params;

    @JsonCreator
    public Signature(
            @JsonProperty("type") Type type,
            @JsonProperty("params") Map<String, Type> params)
    {
        this.type = type;
        this.params = ImmutableMap.copyOf(checkOrdered(params));
    }

    @Override
    public String toString()
    {
        return "Signature{" +
                "type=" + type +
                ", params=" + params +
                '}';
    }

    @JsonProperty("type")
    public Type getType()
    {
        return type;
    }

    @JsonProperty("params")
    public Map<String, Type> getParams()
    {
        return params;
    }
}
