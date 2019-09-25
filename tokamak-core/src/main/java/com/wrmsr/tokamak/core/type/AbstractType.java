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

package com.wrmsr.tokamak.core.type;

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public abstract class AbstractType
        implements Type
{
    protected final String name;
    protected final OptionalInt fixedSize;

    public AbstractType(String name, OptionalInt fixedSize)
    {
        this.name = checkNotEmpty(name);
        this.fixedSize = checkNotNull(fixedSize);
    }

    public AbstractType(String name, int fixedSize)
    {
        this(name, OptionalInt.of(fixedSize));
    }

    public AbstractType(String name)
    {
        this(name, OptionalInt.empty());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public OptionalInt getFixedSize()
    {
        return fixedSize;
    }
}
