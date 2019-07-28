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

package com.wrmsr.tokamak;

import com.wrmsr.tokamak.jdbc.metadata.ColumnMetaData;
import com.wrmsr.tokamak.type.Type;

import java.sql.Types;

public final class TypeUtils
{
    private TypeUtils()
    {
    }

    public static Type getTypeForColumn(ColumnMetaData cmd)
    {
        switch (cmd.getDataType()) {
            case Types.BIT:
                return Type.BOOLEAN;

            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return Type.LONG;

            case Types.FLOAT:
            case Types.DOUBLE:
                return Type.DOUBLE;

            case Types.CLOB:
            case Types.VARCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCLOB:
            case Types.NVARCHAR:
                return Type.STRING;

            case Types.BLOB:
            case Types.VARBINARY:
                return Type.BYTES;

            default:
                throw new IllegalArgumentException(cmd.toString());
        }
    }
}
