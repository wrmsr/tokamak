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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseConfigPropertyImpl<T>
        implements BaseConfigProperty<T>
{
    private final ConfigPropertyMetadata metadata;

    public BaseConfigPropertyImpl(ConfigPropertyMetadata metadata)
    {
        this.metadata = checkNotNull(metadata);
    }

    private final List<Consumer<T>> validators = new CopyOnWriteArrayList<>();
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Type type()
    {
        return metadata.getType();
    }

    @Override
    public String name()
    {
        return metadata.getName();
    }

    @Override
    public Optional<String> doc()
    {
        return metadata.getDoc();
    }

    protected abstract void _set(T value);

    @Override
    public void set(T value)
    {
        validate(value);
        _set(value);
        update(value);
    }

    @Override
    public void validate(T value)
    {
        for (Consumer<T> validator : validators) {
            validator.accept(value);
        }
    }

    @Override
    public void addValidator(Consumer<T> validator)
    {
        validators.add(checkNotNull(validator));
    }

    protected void update(T value)
    {
        for (Consumer<T> listener : listeners) {
            listener.accept(value);
        }
    }

    @Override
    public void addListener(Consumer<T> listener)
    {
        listeners.add(checkNotNull(listener));
    }
}
