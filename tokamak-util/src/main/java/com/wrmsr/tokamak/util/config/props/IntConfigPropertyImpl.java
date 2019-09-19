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

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class IntConfigPropertyImpl
        extends BaseConfigPropertyImpl<Integer>
        implements IntConfigProperty
{
    private final IntSupplier getter;
    private final IntConsumer setter;

    public IntConfigPropertyImpl(ConfigPropertyMetadata metadata, IntSupplier getter, IntConsumer setter)
    {
        super(metadata);
        checkArgument(metadata.getType() == int.class);
        this.getter = checkNotNull(getter);
        this.setter = checkNotNull(setter);
        init();
    }

    @Override
    public Integer getObj()
    {
        return get();
    }

    @Override
    public int get()
    {
        return getter.getAsInt();
    }

    @Override
    protected void _set(Integer value)
    {
        setter.accept(value);
    }
}
