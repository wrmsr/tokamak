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

public final class NullableRowIdCodec
        implements RowIdCodec
{
    private final RowIdCodec child;

    public NullableRowIdCodec(RowIdCodec child)
    {
        this.child = checkNotNull(child);
    }

    @Override
    public Map<String, Object> decode(byte[] data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encode(Map<String, Object> data)
    {
        byte[] childData = child.encode(data);
        if (childData == null) {
            return new byte[] {(byte) 0};
        }
        else {
            byte[] buf = new byte[childData.length + 1];
            buf[0] = (byte) 1;
            System.arraycopy(childData, 0, buf, 1, childData.length);
            return buf;
        }
    }
}
