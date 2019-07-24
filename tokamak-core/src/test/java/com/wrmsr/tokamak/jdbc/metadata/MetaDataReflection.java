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

import com.wrmsr.tokamak.jdbc.JdbcUtils;
import com.wrmsr.tokamak.jdbc.TableIdentifier;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class MetaDataReflection
{
    private MetaDataReflection()
    {
    }

    public static List<TableMetaData> getTableMetadata(DatabaseMetaData metaData)
            throws SQLException
    {
        return JdbcUtils.readRows(
                metaData.getTables(
                        null,
                        null,
                        "%",
                        new String[] {"TABLE"}))
                .stream().map(TableMetaData::new).collect(toImmutableList());
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
                .stream().map(ColumnMetaData::new).collect(toImmutableList());
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
                .stream().map(IndexMetaData::new).collect(toImmutableList());
    }

    public static List<CompositeIndexMetaData> getCompositeIndexMetaData(DatabaseMetaData metaData, TableIdentifier tableIdentifier)
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
}
