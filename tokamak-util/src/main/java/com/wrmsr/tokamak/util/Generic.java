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

package com.wrmsr.tokamak.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.TypeLiteral;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkState;

@Immutable
public class Generic<T>
{
    protected final Type type;

    protected Generic()
    {
        Type superClass = getClass().getGenericSuperclass();
        checkState(!(superClass instanceof Class<?>));
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    @Override
    public String toString()
    {
        return "Generic{" +
                "type=" + type +
                '}';
    }

    public Type getType()
    {
        return type;
    }

    @SuppressWarnings({"unchecked"})
    public TypeLiteral<T> toJackson()
    {
        return (TypeLiteral<T>) TypeLiteral.get(type);
    }

    public TypeReference<T> toGuice()
    {
        return new TypeReference<T>()
        {
            @Override
            public Type getType()
            {
                return type;
            }
        };
    }
}
