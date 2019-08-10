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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Catalog
{
    private final Map<String, Connector> connectorsByName = new HashMap<>();
    private final Map<String, Schema> schemasByName = new HashMap<>();

    public Map<String, Connector> getConnectorsByName()
    {
        return Collections.unmodifiableMap(connectorsByName);
    }

    public Map<String, Schema> getSchemasByName()
    {
        return Collections.unmodifiableMap(schemasByName);
    }

    public Table getTable(SchemaTable schemaTable)
    {
        Schema schema = schemasByName.get(schemaTable.getSchema());
        if (schema == null) {
            throw new IllegalArgumentException("Schema not found: " + schemaTable);
        }
        Table table = schema.getTablesByName().get(schemaTable.getTable());
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + schemaTable);
        }
        return table;
    }
}
