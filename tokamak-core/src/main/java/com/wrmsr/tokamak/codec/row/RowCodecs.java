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

import com.wrmsr.tokamak.codec.scalar.ScalarCodecs;
import com.wrmsr.tokamak.type.Type;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;

public final class RowCodecs
{
    private RowCodecs()
    {
    }

    public static ScalarRowCodec buildRowCodec(String field, Type type)
    {
        return new ScalarRowCodec(field, ScalarCodecs.SCALAR_CODECS_BY_TYPE.get(type));
    }

    public static RowCodec buildRowCodec(Map<String, Type> typesByField)
    {
        checkArgument(!typesByField.isEmpty());
        if (typesByField.size() == 1) {
            Map.Entry<String, Type> e = typesByField.entrySet().stream().collect(toSingle());
            return buildRowCodec(e.getKey(), e.getValue());
        }
        else {
            List<RowCodec> parts = typesByField.entrySet().stream()
                    .map(e -> buildRowCodec(e.getKey(), e.getValue()))
                    .collect(toImmutableList());
            return new CompositeRowCodec(parts);
        }
    }
}
