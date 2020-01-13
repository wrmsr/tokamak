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
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistrant;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class SizedType
        extends SigilType
{
    public static final String NAME = "Sized";
    public static final TypeRegistrant REGISTRANT = new TypeRegistrant(NAME, SizedType.class, TypeConstructor.of(
            (List<Object> args) -> {
                checkArgument(args.size() == 2);
                return new SizedType((Type) args.get(0), (long) args.get(1));
            }));

    public SizedType(Type itemType, long size)
    {
        super(NAME, itemType, ImmutableList.of(size), ImmutableMap.of());
    }

    public long getSize()
    {
        return (long) args.get(1);
    }
}
