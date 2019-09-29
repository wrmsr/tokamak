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
package com.wrmsr.tokamak.core.driver.build.ops;

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;

import javax.annotation.concurrent.Immutable;

import java.util.Collection;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class RequestBuildOp
        implements BuildOp
{
    private final Builder builder;
    private final Key key;
    private final Consumer<Collection<DriverRow>> callback;

    public RequestBuildOp(Builder builder, Key key, Consumer<Collection<DriverRow>> callback)
    {
        this.builder = checkNotNull(builder);
        this.key = checkNotNull(key);
        this.callback = checkNotNull(callback);
    }

    public Builder getBuilder()
    {
        return builder;
    }

    public Key getKey()
    {
        return key;
    }

    public Consumer<Collection<DriverRow>> getCallback()
    {
        return callback;
    }
}
