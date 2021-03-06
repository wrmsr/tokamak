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
package com.wrmsr.tokamak.api;

import java.util.function.Function;

import static com.wrmsr.tokamak.api.Util.checkNotNull;

public final class JsonConverter<F, T>
{
    private final Class<F> fromCls;
    private final Class<T> toCls;

    private final Function<F, T> toFn;
    private final Function<T, F> fromFn;

    public JsonConverter(Class<F> fromCls, Class<T> toCls, Function<F, T> toFn, Function<T, F> fromFn)
    {
        this.fromCls = checkNotNull(fromCls);
        this.toCls = checkNotNull(toCls);
        this.toFn = checkNotNull(toFn);
        this.fromFn = checkNotNull(fromFn);
    }

    public Class<F> getFromCls()
    {
        return fromCls;
    }

    public Class<T> getToCls()
    {
        return toCls;
    }

    public Function<F, T> getToFn()
    {
        return toFn;
    }

    public Function<T, F> getFromFn()
    {
        return fromFn;
    }
}
