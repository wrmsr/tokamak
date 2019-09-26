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
package com.wrmsr.tokamak.core.type;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public abstract class ArgsType
        extends AbstractType
{
    protected final List<Object> args;

    public ArgsType(String name, List<Object> args)
    {
        super(name);
        this.args = ImmutableList.copyOf(args);
        this.args.forEach(a -> checkArgument(a instanceof Type || a instanceof Long));
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "{" +
                "args=" + args +
                '}';
    }

    public List<Object> getArgs()
    {
        return args;
    }

    @Override
    public final String toRepr()
    {
        return Types.buildArgsRepr(name, args);
    }
}