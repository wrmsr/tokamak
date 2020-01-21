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
package com.wrmsr.tokamak.core.type.hier.special;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.TypeUtils;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;

@Immutable
public final class TupleType
        implements SpecialType
{
    public static final String NAME = "Tuple";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, TupleType.class, TypeConstructor.of(
            (List<Object> args) -> new TupleType(TypeUtils.objectsToTypes(args))));

    private final List<Type> items;

    public TupleType(Iterable<Type> items)
    {
        this.items = ImmutableList.copyOf(items);
    }

    public List<Type> getItems()
    {
        return items;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public List<Object> getArgs()
    {
        return ImmutableList.copyOf(items);
    }
}
