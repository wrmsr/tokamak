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
package com.wrmsr.tokamak.serdes;

import static com.google.common.base.Preconditions.checkArgument;

public final class Serdes
{
    private Serdes()
    {
    }

    public static final class NopSerializer<T>
            implements Serializer<T>
    {
        @Override
        public T decode(SerdesContext ctx, Object data)
        {
            return (T) data;
        }

        @Override
        public Object encode(SerdesContext ctx, T data)
        {
            return data;
        }
    }

    public static <T> Serializer<T> nop()
    {
        return new NopSerializer<>();
    }

    private static final class CheckedSerializer<T>
            implements Serializer<T>
    {
        private final Class<? extends T> cls;

        public CheckedSerializer(Class<? extends T> cls)
        {
            this.cls = cls;
        }

        public Class<? extends T> getCls()
        {
            return cls;
        }

        @Override
        public T decode(SerdesContext ctx, Object data)
        {
            checkArgument(cls.isInstance(data));
            return (T) data;
        }

        @Override
        public Object encode(SerdesContext ctx, T data)
        {
            checkArgument(cls.isInstance(data));
            return data;
        }
    }

    public static <T> Serializer<T> checked(Class<? extends T> cls)
    {
        return new CheckedSerializer<>(cls);
    }
}
