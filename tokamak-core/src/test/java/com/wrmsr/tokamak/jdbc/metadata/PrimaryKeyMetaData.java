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

import java.util.Map;

public final class PrimaryKeyMetaData
{
    private final String tableCatalog;
    private final String tableSchema;
    private final String tableName;
    private final String columnName;
    private final Short ordinalPosition;
    private final String pkName;

    public PrimaryKeyMetaData(Map<String, Object> map)
    {
        tableCatalog = (String) map.get("TABLE_CATALOG");
        tableSchema = (String) map.get("TABLE_SCHEMA");
        tableName = (String) map.get("TABLE_NAME");
        columnName = (String) map.get("COLUMN_NAME");
        ordinalPosition = (Short) map.get("ORDINAL_POSITION");
        pkName = (String) map.get("PK_NAME");
    }

    public String getTableCatalog()
    {
        return tableCatalog;
    }

    public String getTableSchema()
    {
        return tableSchema;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public Short getOrdinalPosition()
    {
        return ordinalPosition;
    }

    public String getPkName()
    {
        return pkName;
    }
}
