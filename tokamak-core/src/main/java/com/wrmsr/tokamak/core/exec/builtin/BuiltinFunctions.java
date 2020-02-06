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
package com.wrmsr.tokamak.core.exec.builtin;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.exec.Reflection;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;

public final class BuiltinFunctions
{
    /*
    TODO:
     - generalized 'transmute' (not internalize) -> identity (not nop), opto out
    */

    private BuiltinFunctions()
    {
    }

    public static long longIdentity(long value)
    {
        return value;
    }

    public static boolean longEq(long left, long right)
    {
        return left == right;
    }

    public static boolean logicalAnd(boolean left, boolean right)
    {
        return left && right;
    }

    public static boolean logicalOr(boolean left, boolean right)
    {
        return left || right;
    }

    public static boolean logicalNot(boolean value)
    {
        return !value;
    }

    public static BuiltinExecutor register(BuiltinExecutor executor)
    {
        try {
            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("longIdentity", long.class),
                            "transmuteInternal",
                            new FunctionType(
                                    Types.Internal(Types.Long()),
                                    ImmutableList.of(
                                            Types.Long()
                                    )
                            ),
                            Executable.Purity.IDENTITY)
            );

            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("longEq", long.class, long.class),
                            "eq",
                            new FunctionType(
                                    Types.Boolean(),
                                    ImmutableList.of(
                                            Types.Long(),
                                            Types.Long()
                                    )
                            ),
                            Executable.Purity.PURE)
            );

            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("logicalAnd", boolean.class, boolean.class),
                            "logicalAnd",
                            new FunctionType(
                                    Types.Boolean(),
                                    ImmutableList.of(
                                            Types.Boolean(),
                                            Types.Boolean()
                                    )
                            ),
                            Executable.Purity.PURE)
            );

            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("logicalOr", boolean.class, boolean.class),
                            "logicalOr",
                            new FunctionType(
                                    Types.Boolean(),
                                    ImmutableList.of(
                                            Types.Boolean(),
                                            Types.Boolean()
                                    )
                            ),
                            Executable.Purity.PURE)
            );

            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("logicalNot", boolean.class),
                            "logicalNot",
                            new FunctionType(
                                    Types.Boolean(),
                                    ImmutableList.of(
                                            Types.Boolean()
                                    )
                            ),
                            Executable.Purity.PURE)
            );
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return executor;
    }
}
