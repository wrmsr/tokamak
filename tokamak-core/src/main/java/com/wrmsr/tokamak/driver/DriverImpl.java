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
package com.wrmsr.tokamak.driver;

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.driver.state.StateStorage;
import com.wrmsr.tokamak.driver.state.StateStorageImpl;
import com.wrmsr.tokamak.jdbc.JdbcLayoutUtils;
import com.wrmsr.tokamak.jdbc.JdbcTableIdentifier;
import com.wrmsr.tokamak.jdbc.metadata.MetaDataReflection;
import com.wrmsr.tokamak.jdbc.metadata.TableDescription;
import com.wrmsr.tokamak.layout.TableLayout;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.plan.Plan;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverImpl
{
    private final Plan plan;
    private final Jdbi jdbi;

    private final StateStorage stateStorage;

    public DriverImpl(Plan plan, Jdbi jdbi)
    {
        this.plan = plan;
        this.jdbi = jdbi;

        stateStorage = new StateStorageImpl();
    }

    public Plan getPlan()
    {
        return plan;
    }

    public Jdbi getJdbi()
    {
        return jdbi;
    }

    private final Map<SchemaTable, TableLayout> tableLayoutsBySchemaTable = new HashMap<>();

    public TableLayout getTableLayout(SchemaTable table)
    {
        if (tableLayoutsBySchemaTable.containsKey(table)) {
            return tableLayoutsBySchemaTable.get(table);
        }

        TableLayout tableLayout = jdbi.withHandle(handle -> {
            try {
                DatabaseMetaData metaData = handle.getConnection().getMetaData();

                TableDescription tableDescription = MetaDataReflection.getTableDescription(
                        metaData, JdbcTableIdentifier.of("TEST.DB", "PUBLIC", table.getTable()));

                return JdbcLayoutUtils.buildTableLayout(tableDescription);
            }
            catch (SQLException e) {
                throw new RuntimeException();
            }
        });

        tableLayoutsBySchemaTable.put(table, tableLayout);
        return tableLayout;
    }

    public LineagePolicy getLineagePolicy()
    {
        return LineagePolicy.MINIMAL;
    }

    public StateStorage getStateStorage()
    {
        return stateStorage;
    }

    public DriverContext createContext(Handle jdbiHandle)
    {
        return new DriverContextImpl(this, jdbiHandle);
    }

    public List<DriverRow> build(DriverContext ctx, Node node, Key key)
    {
        return ctx.build(node, key);
    }
}
