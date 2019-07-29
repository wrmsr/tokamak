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
package com.wrmsr.tokamak.jdbc;

import com.wrmsr.tokamak.jdbc.metadata.ColumnMetaData;
import com.wrmsr.tokamak.jdbc.metadata.IndexMetaData;
import com.wrmsr.tokamak.jdbc.metadata.PrimaryKeyMetaData;
import com.wrmsr.tokamak.jdbc.metadata.TableDescription;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.layout.TableLayout;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public final class JdbcLayoutUtils
{
    private JdbcLayoutUtils()
    {
    }

    public static RowLayout buildRowLayout(TableDescription tableDescription)
    {
        return new RowLayout(
                tableDescription.getColumnMetaDatas().stream().collect(toImmutableMap(
                        ColumnMetaData::getColumnName,
                        JdbcTypeUtils::getTypeForColumn)));
    }

    public static TableLayout buildTableLayout(TableDescription tableDescription)
    {
        RowLayout rowLayout = buildRowLayout(tableDescription);
        return new TableLayout(
                rowLayout,
                new TableLayout.Key(
                        tableDescription.getCompositePrimaryKeyMetaData().stream()
                                .map(PrimaryKeyMetaData::getColumnName)
                                .collect(toImmutableList())),
                tableDescription.getCompositeIndexMetaDatas().stream()
                        .map(imd -> new TableLayout.Key(
                                imd.stream()
                                        .map(IndexMetaData::getColumnName)
                                        .collect(toImmutableList())))
                        .collect(toImmutableList()));
    }
}
