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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.layout.TableLayout;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Table
{
    private final String name;
    private final TableLayout layout;

    private Schema schema;

    public Table(Schema schema, String name, TableLayout layout)
    {
        this.schema = checkNotNull(schema);
        this.name = checkNotNull(name);
        this.layout = checkNotNull(layout);
    }

    @JsonCreator
    private Table(
            @JsonProperty("name") String name,
            @JsonProperty("layout") TableLayout layout)
    {
        this.name = checkNotNull(name);
        this.layout = checkNotNull(layout);
    }

    void setSchema(Schema schema)
    {
        checkState(this.schema == null);
        this.schema = checkNotNull(schema);
    }

    @Override
    public String toString()
    {
        return "Table{" +
                "schema=" + schema +
                ", name='" + name + '\'' +
                '}';
    }

    @JsonProperty("schema")
    public Schema getSchema()
    {
        return schema;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("layout")
    public TableLayout getLayout()
    {
        return layout;
    }

    public SchemaTable getSchemaTable()
    {
        return SchemaTable.of(schema.getName(), name);
    }
}
