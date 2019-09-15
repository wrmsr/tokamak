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
package com.wrmsr.tokamak.util.config.props;

import com.wrmsr.tokamak.util.config.ConfigPropertyMetadata;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfigPropertyImpl<T>
        extends BaseConfigPropertyImpl<T>
        implements ConfigProperty<T>
{
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public ConfigPropertyImpl(ConfigPropertyMetadata metadata, Supplier<T> getter, Consumer<T> setter)
    {
        super(metadata);
        this.getter = checkNotNull(getter);
        this.setter = checkNotNull(setter);
    }

    @Override
    public T get()
    {
        return getter.get();
    }

    @Override
    protected void _set(T value)
    {
        setter.accept(value);
    }
}
