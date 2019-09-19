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
import com.wrmsr.tokamak.util.func.BooleanConsumer;

import java.util.function.BooleanSupplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class BooleanConfigPropertyImpl
        extends BaseConfigPropertyImpl<Boolean>
        implements BooleanConfigProperty
{
    private final BooleanSupplier getter;
    private final BooleanConsumer setter;

    public BooleanConfigPropertyImpl(ConfigPropertyMetadata metadata, BooleanSupplier getter, BooleanConsumer setter)
    {
        super(metadata);
        checkArgument(metadata.getType() == boolean.class);
        this.getter = checkNotNull(getter);
        this.setter = checkNotNull(setter);
        init();
    }

    @Override
    public Boolean getObj()
    {
        return get();
    }

    @Override
    public boolean get()
    {
        return getter.getAsBoolean();
    }

    @Override
    protected void _set(Boolean value)
    {
        setter.accept(value);
    }
}
