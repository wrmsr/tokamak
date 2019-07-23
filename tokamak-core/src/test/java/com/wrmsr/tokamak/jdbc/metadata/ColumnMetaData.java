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

public final class ColumnMetaData
{
    private final String tableCatalog;
    private final String tableSchema;
    private final String tableName;
    private final String columnName;
    private final Integer dataType;
    private final String typeName;
    private final Integer columnSize;
    private final Integer bufferLength;
    private final Integer decimalDigits;
    private final Integer numPrecRadix;
    private final Integer nullable;
    private final String remarks;
    private final String columnDef;
    private final Integer sqlDataType;
    private final Integer sqlDatetimeSub;
    private final Integer charOctetLength;
    private final Integer ordinalPosition;
    private final String isNullable;
    private final String scopeCatalog;
    private final String scopeSchema;
    private final String scopeTable;
    private final Short sourceDataType;
    private final String isAutoIncrement;
    private final String isGeneratedColumn;

    public ColumnMetaData(Map<String, Object> map)
    {
        tableCatalog = (String) map.get("TABLE_CAT");
        tableSchema = (String) map.get("TABLE_SCHEM");
        tableName = (String) map.get("TABLE_NAME");
        columnName = (String) map.get("COLUMN_NAME");
        dataType = (Integer) map.get("DATA_TYPE");
        typeName = (String) map.get("TYPE_NAME");
        columnSize = (Integer) map.get("COLUMN_SIZE");
        bufferLength = (Integer) map.get("BUFFER_LENGTH");
        decimalDigits = (Integer) map.get("DECIMAL_DIGITS");
        numPrecRadix = (Integer) map.get("NUM_PREC_RADIX");
        nullable = (Integer) map.get("NULLABLE");
        remarks = (String) map.get("REMARKS");
        columnDef = (String) map.get("COLUMN_DEF");
        sqlDataType = (Integer) map.get("SQL_DATA_TYPE");
        sqlDatetimeSub = (Integer) map.get("SQL_DATETIME_SUB");
        charOctetLength = (Integer) map.get("CHAR_OCTET_LENGTH");
        ordinalPosition = (Integer) map.get("ORDINAL_POSITION");
        isNullable = (String) map.get("IS_NULLABLE");
        scopeCatalog = (String) map.get("SCOPE_CATALOG");
        scopeSchema = (String) map.get("SCOPE_SCHEMA");
        scopeTable = (String) map.get("SCOPE_TABLE");
        sourceDataType = (Short) map.get("SOURCE_DATA_TYPE");
        isAutoIncrement = (String) map.get("IS_AUTOINCREMENT");
        isGeneratedColumn = (String) map.get("IS_GENERATEDCOLUMN");
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

    public Integer getDataType()
    {
        return dataType;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public Integer getColumnSize()
    {
        return columnSize;
    }

    public Integer getBufferLength()
    {
        return bufferLength;
    }

    public Integer getDecimalDigits()
    {
        return decimalDigits;
    }

    public Integer getNumPrecRadix()
    {
        return numPrecRadix;
    }

    public Integer getNullable()
    {
        return nullable;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public String getColumnDef()
    {
        return columnDef;
    }

    public Integer getSqlDataType()
    {
        return sqlDataType;
    }

    public Integer getSqlDatetimeSub()
    {
        return sqlDatetimeSub;
    }

    public Integer getCharOctetLength()
    {
        return charOctetLength;
    }

    public Integer getOrdinalPosition()
    {
        return ordinalPosition;
    }

    public String getIsNullable()
    {
        return isNullable;
    }

    public String getScopeCatalog()
    {
        return scopeCatalog;
    }

    public String getScopeSchema()
    {
        return scopeSchema;
    }

    public String getScopeTable()
    {
        return scopeTable;
    }

    public Short getSourceDataType()
    {
        return sourceDataType;
    }

    public String getIsAutoIncrement()
    {
        return isAutoIncrement;
    }

    public String getIsGeneratedColumn()
    {
        return isGeneratedColumn;
    }
}
