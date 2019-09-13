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

import javax.annotation.concurrent.Immutable;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class SimpleExecutable
        implements Executable
{
    private final String name;
    private final Signature signature;
    private final Function<Object[], Object> function;

    public SimpleExecutable(String name, Signature signature, Function<Object[], Object> function)
    {
        this.name = checkNotEmpty(name);
        this.signature = checkNotNull(signature);
        this.function = checkNotNull(function);
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

    public Function<Object[], Object> getFunction()
    {
        return function;
    }

    @Override
    public Object invoke(Object... args)
    {
        return function.apply(args);
    }
}