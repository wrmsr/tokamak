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
package com.wrmsr.tokamak.type;

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PrimitiveType<T>
        implements Type
{
    private final Class<T> cls;
    private final OptionalInt fixedSize;

    public PrimitiveType(Class<T> cls, OptionalInt fixedSize)
    {
        this.cls = cls;
        this.fixedSize = checkNotNull(fixedSize);
    }

    @Override
    public String toString()
    {
        return "PrimitiveType{" +
                "cls=" + cls +
                ", fixedSize=" + fixedSize +
                '}';
    }

    @Override
    public OptionalInt getFixedSize()
    {
        return fixedSize;
    }

    public Class<T> getCls()
    {
        return cls;
    }
}
