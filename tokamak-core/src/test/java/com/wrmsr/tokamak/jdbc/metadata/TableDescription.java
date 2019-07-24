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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.jdbc.TableIdentifier;
import com.wrmsr.tokamak.jdbc.metadata.ColumnMetaData;
import com.wrmsr.tokamak.jdbc.metadata.CompositeIndexMetaData;
import com.wrmsr.tokamak.jdbc.metadata.CompositePrimaryKeyMetaData;
import com.wrmsr.tokamak.jdbc.metadata.TableMetaData;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.UnaryOperator.identity;

public class TableDescription
{
    private final TableMetaData tableMetaData;
    private final List<ColumnMetaData> columnMetaDatas;
    private final CompositePrimaryKeyMetaData compositePrimaryKeyMetaData;
    private final List<CompositeIndexMetaData> compositeIndexMetaDatas;

    private final Map<String, ColumnMetaData> columnMetaDataByName;
    private final Map<String, CompositeIndexMetaData> compositeIndexMetaDataByName;

    public TableDescription(
            TableMetaData tableMetaData,
            List<ColumnMetaData> columnMetaDatas,
            CompositePrimaryKeyMetaData compositePrimaryKeyMetaData,
            List<CompositeIndexMetaData> compositeIndexMetaDatas)
    {
        this.tableMetaData = tableMetaData;
        this.columnMetaDatas = ImmutableList.copyOf(columnMetaDatas);
        this.compositePrimaryKeyMetaData = compositePrimaryKeyMetaData;
        this.compositeIndexMetaDatas = ImmutableList.copyOf(compositeIndexMetaDatas);

        columnMetaDataByName = columnMetaDatas.stream().collect(toImmutableMap(ColumnMetaData::getColumnName, identity()));
        compositeIndexMetaDataByName = compositeIndexMetaDatas.stream().collect(toImmutableMap(CompositeIndexMetaData::getIndexName, identity()));
    }

    public TableIdentifier getTableIdentifier()
    {
        return tableMetaData.getTableIdentifier();
    }

    public TableMetaData getTableMetaData()
    {
        return tableMetaData;
    }

    public List<ColumnMetaData> getColumnMetaDatas()
    {
        return columnMetaDatas;
    }

    public CompositePrimaryKeyMetaData getCompositePrimaryKeyMetaData()
    {
        return compositePrimaryKeyMetaData;
    }

    public List<CompositeIndexMetaData> getCompositeIndexMetaDatas()
    {
        return compositeIndexMetaDatas;
    }

    public Map<String, ColumnMetaData> getColumnMetaDataByName()
    {
        return columnMetaDataByName;
    }

    public Map<String, CompositeIndexMetaData> getCompositeIndexMetaDataByName()
    {
        return compositeIndexMetaDataByName;
    }
}
