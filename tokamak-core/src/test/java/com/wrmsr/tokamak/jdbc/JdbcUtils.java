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

import com.google.common.collect.ImmutableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class JdbcUtils
{
    private JdbcUtils()
    {
    }

    public static Map<String, Object> readRow(ResultSet rs)
            throws SQLException
    {
        ResultSetMetaData rmd = rs.getMetaData();
        Map<String, Object> ret = new LinkedHashMap<>();
        for (int i = 1; i <= rmd.getColumnCount(); ++i) {
            ret.put(rmd.getColumnName(i), rs.getObject(i));
        }
        return ret;
    }

    public static List<Map<String, Object>> readRows(ResultSet rs)
            throws SQLException
    {
        ImmutableList.Builder<Map<String, Object>> ret = ImmutableList.builder();
        while (rs.next()) {
            ret.add(readRow(rs));
        }
        return ret.build();
    }

    public static List<Map<String, Object>> execute(Connection conn, String sql)
            throws SQLException
    {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                return readRows(rs);
            }
        }
    }

    public static Object scalar(Connection conn, String sql)
            throws SQLException
    {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                checkState(rs.next());
                checkState(rs.getMetaData().getColumnCount() == 1);
                return rs.getObject(1);
            }
        }
    }

    public static List<String> splitSql(String sql)
    {
        List<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (line.contains("--")) {
                line = line.split("--")[0].trim();
            }
            if (line.contains(";")) {
                int pos = line.indexOf(';');
                sb.append(line.substring(0, pos));
                ret.add(sb.toString());
                sb = new StringBuilder();
                sb.append(line.substring(pos + 1));
            }
            else {
                sb.append(line);
            }
        }
        if (sb.length() > 0) {
            ret.add(sb.toString());
        }
        return ret;
    }
}
