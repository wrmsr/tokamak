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
package com.wrmsr.tokamak.util.sql.metadata;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.sql.SqlTableIdentifier;
import com.wrmsr.tokamak.util.sql.SqlUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class MetaDataReflection
{
    public static final Map<Integer, String> DATA_TYPES = ImmutableMap.<Integer, String>builder()
            .put(Types.BIT, "BIT")
            .put(Types.TINYINT, "TINYINT")
            .put(Types.SMALLINT, "SMALLINT")
            .put(Types.INTEGER, "INTEGER")
            .put(Types.BIGINT, "BIGINT")
            .put(Types.FLOAT, "FLOAT")
            .put(Types.REAL, "REAL")
            .put(Types.DOUBLE, "DOUBLE")
            .put(Types.NUMERIC, "NUMERIC")
            .put(Types.DECIMAL, "DECIMAL")
            .put(Types.CHAR, "CHAR")
            .put(Types.VARCHAR, "VARCHAR")
            .put(Types.LONGVARCHAR, "LONGVARCHAR")
            .put(Types.DATE, "DATE")
            .put(Types.TIME, "TIME")
            .put(Types.TIMESTAMP, "TIMESTAMP")
            .put(Types.BINARY, "BINARY")
            .put(Types.VARBINARY, "VARBINARY")
            .put(Types.LONGVARBINARY, "LONGVARBINARY")
            .put(Types.NULL, "NULL")
            .put(Types.OTHER, "OTHER")
            .put(Types.JAVA_OBJECT, "JAVA_OBJECT")
            .put(Types.DISTINCT, "DISTINCT")
            .put(Types.STRUCT, "STRUCT")
            .put(Types.ARRAY, "ARRAY")
            .put(Types.BLOB, "BLOB")
            .put(Types.CLOB, "CLOB")
            .put(Types.REF, "REF")
            .put(Types.DATALINK, "DATALINK")
            .put(Types.BOOLEAN, "BOOLEAN")
            .put(Types.ROWID, "ROWID")
            .put(Types.NCHAR, "NCHAR")
            .put(Types.NVARCHAR, "NVARCHAR")
            .put(Types.LONGNVARCHAR, "LONGNVARCHAR")
            .put(Types.NCLOB, "NCLOB")
            .put(Types.SQLXML, "SQLXML")
            .put(Types.REF_CURSOR, "REF_CURSOR")
            .put(Types.TIME_WITH_TIMEZONE, "TIME_WITH_TIMEZONE")
            .put(Types.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP_WITH_TIMEZONE")
            .build();

    private MetaDataReflection()
    {
    }

    public static TableMetaData getTableMetadata(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return SqlUtils.readRows(
                metaData.getTables(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName(),
                        new String[] {"TABLE"}))
                .stream()
                .findFirst()
                .map(TableMetaData::new)
                .orElseThrow(RuntimeException::new);
    }

    public static List<TableMetaData> getTableMetadatas(DatabaseMetaData metaData)
            throws SQLException
    {
        return SqlUtils.readRows(
                metaData.getTables(
                        null,
                        null,
                        "%",
                        new String[] {"TABLE"}))
                .stream()
                .map(TableMetaData::new)
                .collect(toImmutableList());
    }

    public static List<ColumnMetaData> getColumnMetaData(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return SqlUtils.readRows(
                metaData.getColumns(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName(),
                        "%"))
                .stream()
                .map(ColumnMetaData::new)
                .collect(toImmutableList());
    }

    public static List<IndexMetaData> getIndexMetaData(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return SqlUtils.readRows(
                metaData.getIndexInfo(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName(),
                        false,
                        false))
                .stream()
                .map(IndexMetaData::new)
                .collect(toImmutableList());
    }

    public static List<CompositeIndexMetaData> getCompositeIndexMetaDatas(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        Map<String, List<IndexMetaData>> idxMdsByName = getIndexMetaData(metaData, tableIdentifier).stream()
                .sorted(Comparator.comparing(IndexMetaData::getIndexName))
                .collect(Collectors.groupingBy(IndexMetaData::getIndexName));
        return idxMdsByName.values().stream()
                .map(l -> {
                    l.sort(Comparator.comparing(IndexMetaData::getOrdinalPosition));
                    return l;
                })
                .map(CompositeIndexMetaData::new)
                .collect(toImmutableList());
    }

    public static List<PrimaryKeyMetaData> getPrimaryKeyMetaData(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return SqlUtils.readRows(
                metaData.getPrimaryKeys(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName()))
                .stream().map(PrimaryKeyMetaData::new).collect(toImmutableList());
    }

    public static CompositePrimaryKeyMetaData getCompositePrimaryKeyMetaData(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return new CompositePrimaryKeyMetaData(
                getPrimaryKeyMetaData(metaData, tableIdentifier).stream()
                        .sorted(Comparator.comparing(PrimaryKeyMetaData::getOrdinalPosition))
                        .collect(toImmutableList()));
    }

    public static TableDescription getTableDescription(DatabaseMetaData metaData, SqlTableIdentifier tableIdentifier)
            throws SQLException
    {
        return new TableDescription(
                getTableMetadata(metaData, tableIdentifier),
                getColumnMetaData(metaData, tableIdentifier),
                getCompositePrimaryKeyMetaData(metaData, tableIdentifier),
                getCompositeIndexMetaDatas(metaData, tableIdentifier)
        );
    }
}
