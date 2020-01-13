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

import com.wrmsr.tokamak.core.type.Type;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NamedType
        extends AbstractType
{
    private final Type type;

    public NamedType(String name, Type type)
    {
        super(name);
        this.type = checkNotNull(type);
    }

    @Override
    public String toString()
    {
        return "NamedType{" +
                "name='" + baseName + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return type.toReflect();
    }

    @Override
    public String toSpec()
    {
        return baseName;
    }
}
