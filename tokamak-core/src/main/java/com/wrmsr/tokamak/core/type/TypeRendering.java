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
package com.wrmsr.tokamak.core.type;

import com.google.common.base.Joiner;
import com.google.common.collect.Streams;
import com.wrmsr.tokamak.core.type.hier.Type;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TypeRendering
{
    private TypeRendering()
    {
    }

    public static String buildSpec(
            String name,
            List<Object> args,
            Map<String, Object> kwargs)
    {
        if (args.isEmpty() && kwargs.isEmpty()) {
            return name;
        }

        List<String> parts = Streams.concat(
                args.stream().map(TypeRendering::buildArgSpec),
                kwargs.entrySet().stream().map(e -> e.getKey() + "=" + buildArgSpec(e.getValue()))
        ).collect(toImmutableList());

        return name + '<' + Joiner.on(", ").join(parts) + '>';
    }

    private static String buildArgSpec(Object v)
    {
        if (v instanceof Type) {
            return ((Type) v).toSpec();
        }
        else if (v instanceof Long) {
            return Long.toString((Long) v);
        }
        else {
            throw new IllegalStateException(Objects.toString(v));
        }
    }
}
