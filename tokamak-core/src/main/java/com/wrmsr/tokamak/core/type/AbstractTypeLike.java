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
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.wrmsr.tokamak.core.type.Types.checkValidArg;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public abstract class AbstractTypeLike
        implements TypeLike
{
    protected final List<Object> args;
    protected final Map<String, Object> kwargs;

    public AbstractTypeLike(
            List<Object> args,
            Map<String, Object> kwargs)
    {
        this.args = ImmutableList.copyOf(args);
        this.args.forEach(Types::checkValidArg);

        this.kwargs = ImmutableMap.copyOf(checkOrdered(kwargs));
        this.kwargs.forEach((k, v) -> {
            checkNotEmpty(k);
            checkValidArg(v);
        });
    }

    public AbstractTypeLike()
    {
        this(ImmutableList.of(), ImmutableMap.of());
    }

    public AbstractTypeLike(List<Object> args)
    {
        this(args, ImmutableMap.of());
    }

    public AbstractTypeLike(Map<String, Object> kwargs)
    {
        this(ImmutableList.of(), kwargs);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "args=" + args +
                ", kwargs=" + kwargs +
                '}';
    }

    @Override
    public List<Object> getArgs()
    {
        return args;
    }

    @Override
    public Map<String, Object> getKwargs()
    {
        return kwargs;
    }
}
