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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.sql.metadata.IndexMetaData;
import com.wrmsr.tokamak.util.sql.metadata.PrimaryKeyMetaData;
import com.wrmsr.tokamak.util.sql.metadata.TableDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class JdbcLayoutUtils
{
    private JdbcLayoutUtils()
    {
    }

    public static RowLayout buildRowLayout(TableDescription tableDescription)
    {
        Set<String> pks;
        if (tableDescription.getCompositePrimaryKeyMetaData() != null) {
            pks = tableDescription.getCompositePrimaryKeyMetaData().stream().map(PrimaryKeyMetaData::getColumnName).collect(toImmutableSet());
        }
        else {
            pks = ImmutableSet.of();
        }

        FieldCollection.Builder builder = FieldCollection.builder();
        tableDescription.getColumnMetaDatas().forEach(cmd -> {
            String name = cmd.getColumnName();
            Type type = JdbcTypeUtils.getTypeForColumn(cmd);
            List<FieldAnnotation> anns = new ArrayList<>();
            if (pks.contains(name)) {
                anns.add(FieldAnnotation.id());
            }
            builder.add(new Field(name, type, anns));
        });
        return new RowLayout(builder.build());
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
