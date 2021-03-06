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
package com.wrmsr.tokamak.main.server.gsh;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TtyFilterOutputStream
        extends FilterOutputStream
{
    public TtyFilterOutputStream(OutputStream out)
    {
        super(out);
    }

    @Override
    public void write(int c)
            throws IOException
    {
        if (c == '\n') {
            super.write(c);
            c = '\r';
        }
        super.write(c);
    }

    @Override
    public void write(byte[] b, int off, int len)
            throws IOException
    {
        for (int i = off; i < len; i++) {
            write(b[i]);
        }
    }
}
