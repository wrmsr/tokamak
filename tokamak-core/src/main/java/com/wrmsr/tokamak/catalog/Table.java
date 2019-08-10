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
package com.wrmsr.tokamak.catalog;

import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.layout.TableLayout;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Table
{
    private final Schema schema;
    private final String name;
    private final TableLayout layout;

    public Table(Schema schema, String name, TableLayout layout)
    {
        this.schema = checkNotNull(schema);
        this.name = checkNotNull(name);
        this.layout = checkNotNull(layout);
    }

    @Override
    public String toString()
    {
        return "Table{" +
                "schema=" + schema +
                ", name='" + name + '\'' +
                '}';
    }

    public Schema getSchema()
    {
        return schema;
    }

    public String getName()
    {
        return name;
    }

    public TableLayout getLayout()
    {
        return layout;
    }

    public SchemaTable getSchemaTable()
    {
        return SchemaTable.of(schema.getName(), name);
    }
}
