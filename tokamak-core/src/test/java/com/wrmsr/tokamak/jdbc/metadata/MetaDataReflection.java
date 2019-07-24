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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.jdbc.JdbcUtils;
import com.wrmsr.tokamak.jdbc.TableIdentifier;

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
    public static final Map<Integer, String> DATA_TYPES;

    static {
        ImmutableMap.Builder<Integer, String> builder = ImmutableMap.builder();

        builder.put(Types.BIT, "BIT");
        builder.put(Types.TINYINT, "TINYINT");
        builder.put(Types.SMALLINT, "SMALLINT");
        builder.put(Types.INTEGER, "INTEGER");
        builder.put(Types.BIGINT, "BIGINT");
        builder.put(Types.FLOAT, "FLOAT");
        builder.put(Types.REAL, "REAL");
        builder.put(Types.DOUBLE, "DOUBLE");
        builder.put(Types.NUMERIC, "NUMERIC");
        builder.put(Types.DECIMAL, "DECIMAL");
        builder.put(Types.CHAR, "CHAR");
        builder.put(Types.VARCHAR, "VARCHAR");
        builder.put(Types.LONGVARCHAR, "LONGVARCHAR");
        builder.put(Types.DATE, "DATE");
        builder.put(Types.TIME, "TIME");
        builder.put(Types.TIMESTAMP, "TIMESTAMP");
        builder.put(Types.BINARY, "BINARY");
        builder.put(Types.VARBINARY, "VARBINARY");
        builder.put(Types.LONGVARBINARY, "LONGVARBINARY");
        builder.put(Types.NULL, "NULL");
        builder.put(Types.OTHER, "OTHER");
        builder.put(Types.JAVA_OBJECT, "JAVA_OBJECT");
        builder.put(Types.DISTINCT, "DISTINCT");
        builder.put(Types.STRUCT, "STRUCT");
        builder.put(Types.ARRAY, "ARRAY");
        builder.put(Types.BLOB, "BLOB");
        builder.put(Types.CLOB, "CLOB");
        builder.put(Types.REF, "REF");
        builder.put(Types.DATALINK, "DATALINK");
        builder.put(Types.BOOLEAN, "BOOLEAN");
        builder.put(Types.ROWID, "ROWID");
        builder.put(Types.NCHAR, "NCHAR");
        builder.put(Types.NVARCHAR, "NVARCHAR");
        builder.put(Types.LONGNVARCHAR, "LONGNVARCHAR");
        builder.put(Types.NCLOB, "NCLOB");
        builder.put(Types.SQLXML, "SQLXML");
        builder.put(Types.REF_CURSOR, "REF_CURSOR");
        builder.put(Types.TIME_WITH_TIMEZONE, "TIME_WITH_TIMEZONE");
        builder.put(Types.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP_WITH_TIMEZONE");

        DATA_TYPES = builder.build();
    }

    private MetaDataReflection()
    {
    }

    public static TableMetaData getTableMetadata(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
            throws SQLException
    {
        return JdbcUtils.readRows(
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
        return JdbcUtils.readRows(
                metaData.getTables(
                        null,
                        null,
                        "%",
                        new String[] {"TABLE"}))
                .stream()
                .map(TableMetaData::new)
                .collect(toImmutableList());
    }

    public static List<ColumnMetaData> getColumnMetaData(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
            throws SQLException
    {
        return JdbcUtils.readRows(
                metaData.getColumns(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName(),
                        "%"))
                .stream()
                .map(ColumnMetaData::new)
                .collect(toImmutableList());
    }

    public static List<IndexMetaData> getIndexMetaData(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
            throws SQLException
    {
        return JdbcUtils.readRows(
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

    public static List<CompositeIndexMetaData> getCompositeIndexMetaDatas(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
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

    public static List<PrimaryKeyMetaData> getPrimaryKeyMetaData(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
            throws SQLException
    {
        return JdbcUtils.readRows(
                metaData.getPrimaryKeys(
                        tableIdentifier.getCatalog(),
                        tableIdentifier.getSchema(),
                        tableIdentifier.getName()))
                .stream().map(PrimaryKeyMetaData::new).collect(toImmutableList());
    }

    public static CompositePrimaryKeyMetaData getCompositePrimaryKeyMetaData(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
            throws SQLException
    {
        return new CompositePrimaryKeyMetaData(
                getPrimaryKeyMetaData(metaData, tableIdentifier).stream()
                        .sorted(Comparator.comparing(PrimaryKeyMetaData::getOrdinalPosition))
                        .collect(toImmutableList()));
    }

    public static TableDescription getTableDescription(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
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
