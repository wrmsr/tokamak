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
package com.wrmsr.tokamak.func;

import com.wrmsr.tokamak.type.Type;

import java.lang.reflect.Method;
import java.util.Objects;

public final class Reflection
{
    private Reflection()
    {
    }

    public static ScalarFunction reflect(Method method)
    {
        Class<?>[] params = method.getParameterTypes();
        if (params.length == 0) {
            return NullaryFunction.of(
                    method.getName(),
                    Type.FROM_JAVA_TYPE.get(method.getReturnType()),
                    () -> {
                        try {
                            return method.invoke(null);
                        }
                        catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        else {
            throw new IllegalArgumentException(Objects.toString(method));
        }
    }
}
