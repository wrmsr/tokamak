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

import javax.annotation.concurrent.Immutable;

import java.util.function.Function;

@Immutable
public final class SimpleExecutable
        implements Executable
{
    private final String name;
    private final Signature signature;
    private final Function<Object[], Object> function;

    public SimpleExecutable(String name, Signature signature, Function<Object[], Object> function)
    {
        this.name = name;
        this.signature = signature;
        this.function = function;
    }

    @Override
    public String toString()
    {
        return "SimpleExecutable{" +
                "name='" + name + '\'' +
                ", signature=" + signature +
                ", function=" + function +
                '}';
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Signature getSignature()
    {
        return signature;
    }

    @Override
    public Object invoke(Object... args)
    {
        return null;
    }
}
