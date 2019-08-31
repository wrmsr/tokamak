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
package com.wrmsr.tokamak.sql.metadata;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.conn.jdbc.JdbcTableIdentifier;

import java.sql.DatabaseMetaData;
import java.util.Map;

public final class IndexMetaData
{
    public static final Map<Short, String> INDEX_TYPES = ImmutableMap.of(
            DatabaseMetaData.tableIndexStatistic, "TABLE_INDEX_STATISTIC",
            DatabaseMetaData.tableIndexClustered, "TABLE_INDEX_CLUSTERED",
            DatabaseMetaData.tableIndexHashed, "TABLE_INDEX_HASHED",
            DatabaseMetaData.tableIndexOther, "TABLE_INDEX_OTHER"
    );

    private final String tableCatalog;
    private final String tableSchema;
    private final String tableName;
    private final Boolean nonUnique;
    private final String indexName;
    private final Short indexType;
    private final Short ordinalPosition;
    private final String columnName;
    private final String ascOrDesc;
    private final Integer cardinality;
    private final Integer pages;
    private final String filterCondition;
    private final Integer sortType;

    public IndexMetaData(Map<String, Object> map)
    {
        tableCatalog = (String) map.get("TABLE_CATALOG");
        tableSchema = (String) map.get("TABLE_SCHEMA");
        tableName = (String) map.get("TABLE_NAME");
        nonUnique = (Boolean) map.get("NON_UNIQUE");
        indexName = (String) map.get("INDEX_NAME");
        indexType = (Short) map.get("INDEX_TYPE");
        ordinalPosition = (Short) map.get("ORDINAL_POSITION");
        columnName = (String) map.get("COLUMN_NAME");
        ascOrDesc = (String) map.get("ASC_OR_DESC");
        cardinality = (Integer) map.get("CARDINALITY");
        pages = (Integer) map.get("PAGES");
        filterCondition = (String) map.get("FILTER_CONDITION");
        sortType = (Integer) map.get("SORT_TYPE");
    }

    @Override
    public String toString()
    {
        return "IndexMetaData{" +
                "tableCatalog='" + tableCatalog + '\'' +
                ", tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", indexName='" + indexName + '\'' +
                ", ordinalPosition=" + ordinalPosition +
                ", columnName='" + columnName + '\'' +
                '}';
    }

    public JdbcTableIdentifier getTableIdentifier()
    {
        return new JdbcTableIdentifier(tableCatalog, tableSchema, tableName);
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

    public Boolean getNonUnique()
    {
        return nonUnique;
    }

    public String getIndexName()
    {
        return indexName;
    }

    public Short getIndexType()
    {
        return indexType;
    }

    public Short getOrdinalPosition()
    {
        return ordinalPosition;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public String getAscOrDesc()
    {
        return ascOrDesc;
    }

    public Integer getCardinality()
    {
        return cardinality;
    }

    public Integer getPages()
    {
        return pages;
    }

    public String getFilterCondition()
    {
        return filterCondition;
    }

    public Integer getSortType()
    {
        return sortType;
    }
}
