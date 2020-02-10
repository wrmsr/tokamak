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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;

@Immutable
public final class RequestBuildOp
        extends AbstractBuildOp
{
    private final Map<Builder<?>, Set<Key>> keysSetsByBuilder;
    private final Consumer<Map<Builder<?>, Map<Key, List<DriverRow>>>> callback;

    public RequestBuildOp(Builder<?> origin, Map<Builder<?>, Set<Key>> keysSetsByBuilder, Consumer<Map<Builder<?>, Map<Key, List<DriverRow>>>> callback)
    {
        super(origin);
        this.keysSetsByBuilder = newImmutableSetMap(keysSetsByBuilder);
        this.callback = checkNotNull(callback);
    }

    public RequestBuildOp(Builder<?> origin, Builder<?> builder, Set<Key> keys, Consumer<Map<Builder<?>, Map<Key, List<DriverRow>>>> callback)
    {
        this(origin, ImmutableMap.of(builder, keys), callback);
    }

    public RequestBuildOp(Builder<?> origin, Builder<?> builder, Key key, Consumer<Map<Builder<?>, Map<Key, List<DriverRow>>>> callback)
    {
        this(origin, builder, ImmutableSet.of(key), callback);
    }

    @Override
    public String toString()
    {
        return "RequestBuildOp{" +
                "keysSetsByBuilder=" + keysSetsByBuilder +
                ", callback=" + callback +
                '}';
    }

    public Map<Builder<?>, Set<Key>> getKeysSetsByBuilder()
    {
        return keysSetsByBuilder;
    }

    public Consumer<Map<Builder<?>, Map<Key, List<DriverRow>>>> getCallback()
    {
        return callback;
    }
}
