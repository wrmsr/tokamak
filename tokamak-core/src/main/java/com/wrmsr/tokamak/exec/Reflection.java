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
package com.wrmsr.tokamak.exec;

import com.wrmsr.tokamak.type.TypeUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class Reflection
{
    /*
    TODO:
     - argnames
    */

    private Reflection()
    {
    }

    private static final AtomicInteger reflectedCount = new AtomicInteger();

    public static Executable reflect(Method method, String name)
    {
        return new SimpleExecutable(
                name,
                new Signature(
                        TypeUtils.fromJavaType(method.getReturnType()),
                        IntStream.range(0, method.getParameterTypes().length).boxed()
                                .collect(toImmutableMap(i -> "arg" + i, i -> TypeUtils.fromJavaType(method.getParameterTypes()[i])))),
                args -> {
                    try {
                        return method.invoke(null, args);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static Executable reflect(Method method)
    {
        return reflect(method, "$reflected$" + reflectedCount.getAndIncrement() + "$" + method.getName());
    }

    public static Executable reflect(Supplier supplier)
    {
        try {
            return reflect(supplier.getClass().getDeclaredMethod("get"));
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
