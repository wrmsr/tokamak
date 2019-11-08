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
package com.wrmsr.tokamak.core.serde.impl;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Width;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

@SuppressWarnings({"rawtypes"})
@Immutable
public final class TupleSerde
        implements Serde<Object[]>
{
    private final List<Serde> children;

    public TupleSerde(List<Serde> children)
    {
        this.children = ImmutableList.copyOf(children);
    }

    public List<Serde> getChildren()
    {
        return children;
    }

    private final SupplierLazyValue<Width> width = new SupplierLazyValue<>();

    @Override
    public Width getWidth()
    {
        return width.get(() -> Width.sum(children.stream().map(Serde::getWidth).collect(toImmutableList())));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void write(Object[] value, Output output)
    {
        checkArgument(value.length == children.size());
        for (int i = 0; i < value.length; ++i) {
            children.get(i).write(value[i], output);
        }
    }

    @Override
    public Object[] read(Input input)
    {
        Object[] ret = new Object[children.size()];
        for (int i = 0; i < children.size(); ++i) {
            ret[i] = children.get(i).read(input);
        }
        return ret;
    }
}
