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

import com.wrmsr.tokamak.util.NoExceptAutoCloseable;

import java.sql.Connection;
import java.sql.SQLException;

import static com.google.common.base.Preconditions.checkNotNull;

public class SqlConnection
        implements NoExceptAutoCloseable
{
    private final SqlEngine engine;
    private final Connection connection;

    public SqlConnection(SqlEngine engine, Connection connection)
    {
        this.engine = checkNotNull(engine);
        this.connection = checkNotNull(connection);
    }

    public SqlEngine getEngine()
    {
        return engine;
    }

    public Connection getConnection()
    {
        return connection;
    }

    @Override
    public void close()
    {
        try {
            connection.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
