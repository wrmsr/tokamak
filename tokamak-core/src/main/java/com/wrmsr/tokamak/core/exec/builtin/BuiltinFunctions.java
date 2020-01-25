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

import java.util.Optional;

public final class BuiltinFunctions
{
    /*
    TODO:
     - generalized 'transmute' (not internalize) -> identity (not nop), opto out
    */

    private BuiltinFunctions()
    {
    }

    private static long transmuteInternal(long value)
    {
        return value;
    }

    public static BuiltinExecutor register(BuiltinExecutor executor)
    {
        try {
            executor.register(
                    Reflection.reflect(
                            BuiltinFunctions.class.getDeclaredMethod("transmuteInternal", long.class),
                            Optional.of("transmuteInternal"),
                            Optional.of(
                                    new FunctionType(
                                    Types.Internal(Types.Long()),
                                    ImmutableList.of(
                                            Types.Long()
                                    )
                            )),
                            Executable.Purity.PURE)
                    );
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return executor;
    }
}
