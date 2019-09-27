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
package com.wrmsr.tokamak.core.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class View
{
    private final String name;
    private final String sql;

    @JsonCreator
    public View(
            @JsonProperty("name") String name,
            @JsonProperty("sql") String sql)
    {
        this.name = checkNotNull(name);
        this.sql = checkNotNull(sql);
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("sql")
    public String getSql()
    {
        return sql;
    }
}
