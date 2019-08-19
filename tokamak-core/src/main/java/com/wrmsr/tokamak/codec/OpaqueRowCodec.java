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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class OpaqueRowCodec
        implements RowCodec
{
    private final RowCodec child;

    private OpaqueRowCodec(RowCodec child)
    {
        this.child = checkNotNull(child);
        checkArgument(!(child instanceof OpaqueRowCodec));
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        throw new IllegalStateException();
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        throw new IllegalStateException();
    }

    public static RowCodec of(RowCodec child)
    {
        if (child instanceof OpaqueRowCodec) {
            return child;
        }
        else {
            return new OpaqueRowCodec(child);
        }
    }
}
