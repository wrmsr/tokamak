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

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

public interface BaseConfigProperty<T>
{
    Type type();

    String name();

    Optional<String> doc();

    T getObj();

    void set(T value);

    void validate(T value);

    void addValidator(Consumer<T> validator);

    void addListener(Consumer<T> listener);
}
