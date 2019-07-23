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

import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

public final class TableMetaData
{
    public static final Set<String> TABLE_TYPES = ImmutableSet.of(
            "TABLE",
            "VIEW",
            "SYSTEM TABLE",
            "GLOBAL TEMPORARY",
            "LOCAL TEMPORARY",
            "ALIAS",
            "SYNONYM"
    );

    private final String tableCatalog;
    private final String tableSchema;
    private final String tableName;
    private final String tableType;
    private final String remarks;
    private final String typeName;
    private final String sql;

    public TableMetaData(Map<String, Object> map)
    {
        tableCatalog = (String) map.get("TABLE_CATALOG");
        tableSchema = (String) map.get("TABLE_SCHEMA");
        tableName = (String) map.get("TABLE_NAME");
        tableType = (String) map.get("TABLE_TYPE");
        remarks = (String) map.get("REMARKS");
        typeName = (String) map.get("TYPE_NAME");
        sql = (String) map.get("SQL");
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

    public String getTableType()
    {
        return tableType;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public String getSql()
    {
        return sql;
    }
}
