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

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ResponseBuildOp
        extends AbstractBuildOp
{
    private final Key key;
    private final Collection<DriverRow> rows;

    public ResponseBuildOp(Builder<?> origin, Key key, Collection<DriverRow> rows)
    {
        super(origin);
        this.key = checkNotNull(key);
        this.rows = checkNotNull(rows);
    }

    @Override
    public String toString()
    {
        return "ResponseBuildOp{" +
                "origin=" + origin +
                ", key=" + key +
                '}';
    }

    public Key getKey()
    {
        return key;
    }

    public Collection<DriverRow> getRows()
    {
        return rows;
    }
}
