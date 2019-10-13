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
package com.wrmsr.tokamak.core.exec;

import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.impl.FunctionType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.func.ThrowableThrowingSupplier.throwableRethrowingGet;
import static com.wrmsr.tokamak.util.func.ThrowingSupplier.rethrowingGet;

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

    public static Executable reflectWithoutHandle(Method method, String name)
    {
        return new SimpleExecutable(
                name,
                new FunctionType(
                        Types.fromJavaType(method.getReturnType()),
                        Arrays.stream(method.getParameterTypes()).map(Types::fromJavaType).collect(toImmutableList())),
                args -> rethrowingGet(() -> method.invoke(args)));
    }

    public static Executable reflectWithHandle(Method method, String name)
            throws IllegalAccessException
    {
        MethodHandle handle = MethodHandles.lookup().unreflect(method);
        return new SimpleExecutable(
                name,
                new FunctionType(
                        Types.fromJavaType(method.getReturnType()),
                        Arrays.stream(method.getParameterTypes()).map(Types::fromJavaType).collect(toImmutableList())),
                args -> throwableRethrowingGet(() -> handle.invokeWithArguments(args)));
    }

    public static Executable reflect(Method method, String name)
    {
        try {
            return reflectWithHandle(method, name);
        }
        catch (IllegalAccessException e) {
            return reflectWithoutHandle(method, name);
        }
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
