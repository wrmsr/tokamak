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
package com.wrmsr.tokamak.core.type.impl;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PrimitiveType<T>
        extends AbstractType
{
    private final Class<T> cls;
    private final Class<?> primCls;

    public PrimitiveType(String name, Class<T> cls, Class<?> primCls, int fixedSize)
    {
        super(name, fixedSize);
        this.cls = checkNotNull(cls);
        this.primCls = checkNotNull(primCls);
    }

    @Override
    public String toString()
    {
        return "PrimitiveType{" +
                "cls=" + cls +
                ", primCls=" + primCls +
                '}';
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return cls;
    }

    public Class<T> getCls()
    {
        return cls;
    }

    public Class<?> getPrimCls()
    {
        return primCls;
    }
}
