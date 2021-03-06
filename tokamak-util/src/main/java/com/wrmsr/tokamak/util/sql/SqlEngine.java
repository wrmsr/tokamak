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
package com.wrmsr.tokamak.util.sql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wrmsr.tokamak.util.NoExceptAutoCloseable;

import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public class SqlEngine
        implements NoExceptAutoCloseable
{
    /*
    TODO:
     - dialect
    */

    private final String url;

    @JsonCreator
    public SqlEngine(
            @JsonProperty("url") String url)
    {
        this.url = checkNotNull(url);
    }

    @JsonProperty("url")
    public String getUrl()
    {
        return url;
    }

    public SqlConnection connect()
    {
        try {
            return new SqlConnection(this, DriverManager.getConnection(url));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
