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
package com.wrmsr.tokamak.core.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.DesigiledType;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeRendering;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.core.type.Types.checkValidArg;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public abstract class AbstractType
        implements Type
{
    protected final String baseName;
    protected final OptionalInt fixedSize;

    protected final List<Object> args;
    protected final Map<String, Object> kwargs;

    public AbstractType(
            String baseName,
            OptionalInt fixedSize,
            List<Object> args,
            Map<String, Object> kwargs)
    {
        this.baseName = checkNotEmpty(baseName);
        this.fixedSize = checkNotNull(fixedSize);

        this.args = ImmutableList.copyOf(args);
        this.args.forEach(Types::checkValidArg);

        this.kwargs = ImmutableMap.copyOf(checkOrdered(kwargs));
        this.kwargs.forEach((k, v) -> {
            checkNotEmpty(k);
            checkValidArg(v);
        });
    }

    public AbstractType(String baseName, OptionalInt fixedSize)
    {
        this(baseName, fixedSize, ImmutableList.of(), ImmutableMap.of());
    }

    public AbstractType(String baseName, int fixedSize)
    {
        this(baseName, OptionalInt.of(fixedSize));
    }

    public AbstractType(String baseName)
    {
        this(baseName, OptionalInt.empty());
    }

    public AbstractType(String baseName, List<Object> args)
    {
        this(baseName, OptionalInt.empty(), args, ImmutableMap.of());
    }

    public AbstractType(String baseName, Map<String, Object> kwargs)
    {
        this(baseName, OptionalInt.empty(), ImmutableList.of(), kwargs);
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
    public String getBaseName()
    {
        return baseName;
    }

    @Override
    public OptionalInt getFixedSize()
    {
        return fixedSize;
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

    @Override
    public final String toSpec()
    {
        return TypeRendering.buildSpec(baseName, args, kwargs);
    }

    private final SupplierLazyValue<DesigiledType> desigiled = new SupplierLazyValue<>();

    @Override
    public DesigiledType desigil()
    {
        return desigiled.get(() -> new DesigiledType(this));
    }
}
