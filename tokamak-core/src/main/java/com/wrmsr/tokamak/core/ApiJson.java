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
package com.wrmsr.tokamak.core;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.JsonConverter;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.api.SimpleRow;
import com.wrmsr.tokamak.util.codec.Codec;
import com.wrmsr.tokamak.util.json.CodecSerialization;
import com.wrmsr.tokamak.util.json.Json;

import java.util.List;
import java.util.function.Supplier;

public final class ApiJson
{
    private ApiJson()
    {
    }

    private static final List<JsonConverter> JSON_CONVERTERS = ImmutableList.copyOf(new JsonConverter[] {
            Id.JSON_CONVERTER,
            Key.JSON_CONVERTER,
            SchemaTable.JSON_CONVERTER,
            SimpleRow.JSON_CONVERTER,
    });

    private static <F, T> CodecSerialization<F, T> buildSerialization(JsonConverter<F, T> converter)
    {
        return new CodecSerialization<F, T>(
                converter.getFromCls(),
                converter.getToCls(),
                Codec.of(converter.getToFn()::apply, converter.getFromFn()::apply));
    }

    @SuppressWarnings({"unchecked"})
    private static final Supplier<Module> MODULE_SUPPLIER = () -> {
        SimpleModule mod = new SimpleModule();
        JSON_CONVERTERS.forEach(jc -> buildSerialization(jc).install(mod));
        return mod;
    };

    private static final Object lock = new Object();

    public static void installStatics()
    {
        synchronized (lock) {
            if (!Json.DEFAULT_MODULE_FACTORIES.contains(MODULE_SUPPLIER)) {
                Json.DEFAULT_MODULE_FACTORIES.add(MODULE_SUPPLIER);
            }
        }
    }
}
