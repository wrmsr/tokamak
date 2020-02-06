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
package com.wrmsr.tokamak.core.conn.jdbc;

import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.util.sql.metadata.ColumnMetaData;

import java.sql.Types;
import java.util.Objects;

public final class JdbcTypeUtils
{
    private JdbcTypeUtils()
    {
    }

    public static Type getTypeForColumn(ColumnMetaData cmd)
    {
        switch (cmd.getDataType()) {
            case Types.BIT:
                return com.wrmsr.tokamak.core.type.Types.Boolean();

            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return com.wrmsr.tokamak.core.type.Types.Long();

            case Types.FLOAT:
            case Types.REAL:

            case Types.DOUBLE:
                return com.wrmsr.tokamak.core.type.Types.Double();

            case Types.DECIMAL:
                return com.wrmsr.tokamak.core.type.Types.BigDecimal();

            case Types.DATE:
                return com.wrmsr.tokamak.core.type.Types.LocalDateTime();

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:

            case Types.CLOB:

            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:

            case Types.NCLOB:
                return com.wrmsr.tokamak.core.type.Types.String();

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:

            case Types.BLOB:
                return com.wrmsr.tokamak.core.type.Types.Bytes();

            default:
                throw new IllegalArgumentException(Objects.toString(cmd));
        }
    }
}
