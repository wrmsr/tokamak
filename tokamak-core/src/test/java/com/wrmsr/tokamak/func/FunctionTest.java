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
import junit.framework.TestCase;

import java.lang.reflect.Method;

public class FunctionTest
        extends TestCase
{
    public void testReflection()
            throws Throwable
    {
        Function f = RowFunction.anon(Type.LONG, r -> 0L);
        System.out.println(f);

        Method method = System.class.getDeclaredMethod("currentTimeMillis");
        ScalarFunction function = Reflection.reflect(method);
        System.out.println(function);
        System.out.println(function.invoke());
    }
}