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
package com.wrmsr.tokamak.serde.row;

import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.serde.value.ValueSerde;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ValueRowSerde<V>
        implements RowSerde
{
    private final String field;
    private final ValueSerde<V> valueSerde;

    public ValueRowSerde(String field, ValueSerde<V> valueSerde)
    {
        this.field = checkNotNull(field);
        this.valueSerde = checkNotNull(valueSerde);
    }

    public String getField()
    {
        return field;
    }

    public ValueSerde<V> getValueSerde()
    {
        return valueSerde;
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        valueSerde.write((V) row.get(field), output);
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        sink.put(field, valueSerde.read(input));
    }
}