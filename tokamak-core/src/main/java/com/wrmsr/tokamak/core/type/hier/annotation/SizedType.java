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
package com.wrmsr.tokamak.core.type.hier.annotation;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class SizedType
        implements TypeAnnotation
{
    public static final String NAME = "Sized";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, SizedType.class, TypeConstructor.of(
            (List<Object> args) -> {
                checkArgument(args.size() == 1);
                return new SizedType((long) args.get(0));
            }));

    private final long size;

    public SizedType(long size)
    {
        this.size = size;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public List<Object> getArgs()
    {
        return ImmutableList.of(size);
    }

    public long getSize()
    {
        return size;
    }
}
