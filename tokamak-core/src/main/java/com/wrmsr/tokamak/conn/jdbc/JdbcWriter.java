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
package com.wrmsr.tokamak.conn.jdbc;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.Writer;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.util.List;

public class JdbcWriter
        implements Writer<JdbcWriterTarget>
{
    @Override
    public JdbcWriterTarget getTarget()
    {
        return null;
    }

    @Override
    public void write(List<Row> rows)
            throws IOException
    {

    }

    @Override
    public void writeRange(Span<Id> idSpan, List<Row> rows)
            throws IOException
    {

    }

    @Override
    public void close()
            throws Exception
    {

    }
}