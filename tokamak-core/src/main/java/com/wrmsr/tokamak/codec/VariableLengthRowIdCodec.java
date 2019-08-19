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
import static com.google.common.base.Preconditions.checkState;

public final class VariableLengthRowIdCodec
        implements RowIdCodec
{
    public static final int MAX_ID_LENGTH = 254;

    private final RowIdCodec child;

    public VariableLengthRowIdCodec(RowIdCodec child)
    {
        this.child = checkNotNull(child);
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        int pos = output.tell();
        output.alloc(1);
        child.encode(row, output);
        int sz = output.tell() - pos;
        checkState(sz < MAX_ID_LENGTH);
        output.putAt(pos, (byte) sz);
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        int sz = input.get();
        child.decode(sink, input.nest(sz));
    }
}
