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

import com.google.common.base.Joiner;
import io.airlift.tpch.GenerateUtils;
import io.airlift.tpch.TpchColumn;
import io.airlift.tpch.TpchEntity;
import io.airlift.tpch.TpchTable;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TpchUtils
{
    private TpchUtils()
    {
    }

    public static <E extends TpchEntity> Object getColumnValue(TpchColumn column, E entity)
    {
        switch (column.getType().getBase()) {
            case INTEGER:
                return column.getInteger(entity);
            case IDENTIFIER:
                return column.getIdentifier(entity);
            case DATE:
                return GenerateUtils.formatDate(column.getDate(entity));
            case DOUBLE:
                return column.getDouble(entity);
            case VARCHAR:
                return column.getString(entity);
            default:
                throw new IllegalArgumentException(column.getType().toString());
        }
    }

    public static <E extends TpchEntity> void insertEntities(Handle handle, TpchTable<E> table, Iterable<E> entities)
    {
        String stmt = String.format(
                "insert into %s (%s) values (%s)",
                table.getTableName(),
                Joiner.on(", ").join(
                        table.getColumns().stream().map(TpchColumn::getColumnName).collect(toImmutableList())),
                Joiner.on(", ").join(
                        IntStream.range(0, table.getColumns().size()).mapToObj(i -> "?").collect(toImmutableList())));

        for (E entity : entities) {
            List<Object> f = table.getColumns().stream()
                    .map(c -> getColumnValue(c, entity))
                    .collect(toImmutableList());

            handle.execute(stmt, f.toArray());
        }
    }
}
