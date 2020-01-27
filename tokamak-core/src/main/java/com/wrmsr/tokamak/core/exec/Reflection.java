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
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

    private static String getNextReflectedName(Method method)
    {
        return "$reflected$" + reflectedCount.getAndIncrement() + "$" + method.getName();
    }

    public static Type getValueType(Method method)
    {
        return Types.BUILTIN_REGISTRY.fromReflect(method.getReturnType());
    }

    public static List<Type> getParamTypes(Method method)
    {
        return Arrays.stream(method.getParameterTypes()).map(Types.BUILTIN_REGISTRY::fromReflect).collect(toImmutableList());
    }

    private static Function<Object[], Object> reflectWithoutHandle(Method method, String name, FunctionType type)
    {
        return args -> rethrowingGet(() -> method.invoke(args));
    }

    private static Function<Object[], Object> reflectWithHandle(Method method, String name, FunctionType type)
            throws IllegalAccessException
    {
        MethodHandle handle = MethodHandles.lookup().unreflect(method);
        return args -> throwableRethrowingGet(() -> handle.invokeWithArguments(args));
    }

    public static Executable reflect(
            Method method,
            Optional<String> optName,
            Optional<FunctionType> optType,
            Executable.Purity purity)
    {
        String name = optName.orElseGet(() -> getNextReflectedName(method));
        FunctionType type = optType.orElseGet(() -> new FunctionType(getValueType(method), getParamTypes(method)));

        Function<Object[], Object> function;
        try {
            function = reflectWithHandle(method, name, type);
        }
        catch (IllegalAccessException e) {
            function = reflectWithoutHandle(method, name, type);
        }

        return new SimpleExecutable(
                name,
                type,
                purity,
                function);
    }

    public static Executable reflect(
            Method method,
            String name,
            FunctionType type,
            Executable.Purity purity)
    {
        return reflect(method, Optional.of(name), Optional.of(type), purity);
    }

    public static Executable reflect(
            Method method,
            String name,
            FunctionType type)
    {
        return reflect(method, Optional.of(name), Optional.of(type), Executable.Purity.IMPURE);
    }

    public static Executable reflect(Method method, String name)
    {
        return reflect(method, Optional.of(name), Optional.empty(), Executable.Purity.IMPURE);
    }

    public static Executable reflect(Method method)
    {
        return reflect(method, Optional.empty(), Optional.empty(), Executable.Purity.IMPURE);
    }

    public static Executable reflect(Supplier<?> supplier)
    {
        try {
            return reflect(supplier.getClass().getDeclaredMethod("get"));
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
