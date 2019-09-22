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
package com.wrmsr.tokamak.core.conn.kv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.util.kv.Kv;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class KvTable
{
    private final SchemaTable schemaTable;
    private final TableLayout tableLayout;
    private final Kv<byte[], byte[]> kv;

    @JsonCreator
    public KvTable(
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("tableLayout") TableLayout tableLayout,
            @JsonProperty("kv") Kv<byte[], byte[]> kv)
    {
        this.schemaTable = checkNotNull(schemaTable);
        this.tableLayout = checkNotNull(tableLayout);
        this.kv = checkNotNull(kv);
    }

    @JsonProperty("schemaTable")
    public SchemaTable getSchemaTable()
    {
        return schemaTable;
    }

    @JsonProperty("tableLayout")
    public TableLayout getTableLayout()
    {
        return tableLayout;
    }

    @JsonProperty("kv")
    public Kv<byte[], byte[]> getKv()
    {
        return kv;
    }

    public List<Map<String, Object>> scan(Set<String> fields, Key key)
    {
        checkArgument(tableLayout.getRowLayout().getFields().keySet().containsAll(fields));
        checkArgument(tableLayout.getRowLayout().getFields().keySet().containsAll(key.getFields()));

        ImmutableList.Builder<Map<String, Object>> builder = ImmutableList.builder();

        throw new IllegalStateException();

        // rowLoop:
        // for (Object[] row : rows) {
        //     if (key instanceof FieldKey) {
        //         FieldKey fieldKey = (FieldKey) key;
        //         for (Map.Entry<String, Object> keyEntry : fieldKey.getValuesByField().entrySet()) {
        //             if (!Objects.equals(row[tableLayout.getRowLayout().getPositionsByField().get(keyEntry.getKey())], ((FieldKey) key).get(keyEntry.getKey()))) {
        //                 continue rowLoop;
        //             }
        //         }
        //     }
        //     builder.add(tableLayout.getRowLayout().arrayToMap(row));
        // }
        // return builder.build();
    }
}
