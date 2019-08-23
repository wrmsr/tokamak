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
package com.wrmsr.tokamak.codec.row;

import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.codec.scalar.ScalarCodec;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ScalarRowCodec<V>
        implements RowCodec
{
    private final String field;
    private final ScalarCodec<V> scalarCodec;

    public ScalarRowCodec(String field, ScalarCodec<V> scalarCodec)
    {
        this.field = checkNotNull(field);
        this.scalarCodec = checkNotNull(scalarCodec);
    }

    public String getField()
    {
        return field;
    }

    public ScalarCodec<V> getScalarCodec()
    {
        return scalarCodec;
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        scalarCodec.encode((V) row.get(field), output);
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        sink.put(field, scalarCodec.decode(input));
    }
}
