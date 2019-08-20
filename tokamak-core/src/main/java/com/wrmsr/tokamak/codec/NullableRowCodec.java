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
package com.wrmsr.tokamak.codec;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NullableRowCodec
        implements RowCodec
{
    private final String field;
    private final RowCodec child;

    public NullableRowCodec(String field, RowCodec child)
    {
        this.field = checkNotNull(field);
        this.child = checkNotNull(child);
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        if (row.get(field) != null) {
            output.putLong(0);
            child.encode(row, output);
        }
        else {
            output.putLong(1);
        }
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        if (input.get() == 0) {
            child.decode(sink, input);
        }
    }
}
