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
package com.wrmsr.tokamak.util.java.lang;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

@Immutable
public final class JTypeSpecifier
{
    private final JName name;
    private final Optional<List<JTypeSpecifier>> generics;
    private final List<JArray> arrays;

    public JTypeSpecifier(JName name, Optional<List<JTypeSpecifier>> generics, List<JArray> arrays)
    {
        this.name = checkNotNull(name);
        this.generics = checkNotNull(generics).map(ImmutableList::copyOf);
        this.arrays = ImmutableList.copyOf(arrays);
    }

    public static JTypeSpecifier of(JName name)
    {
        return new JTypeSpecifier(name, Optional.empty(), ImmutableList.of());
    }

    public static JTypeSpecifier of(String... parts)
    {
        return new JTypeSpecifier(JName.of((Object[]) parts), Optional.empty(), ImmutableList.of());
    }

    public static JTypeSpecifier of(Type type)
    {
        if (type instanceof Class) {
            return new JTypeSpecifier(JName.of(type), Optional.empty(), ImmutableList.of());
        }
        else if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            return new JTypeSpecifier(
                    JName.of(ptype.getRawType()),
                    Optional.of(immutableMapItems(Arrays.stream(ptype.getActualTypeArguments()), JTypeSpecifier::of)),
                    ImmutableList.of());
        }
        else {
            throw new IllegalArgumentException(type.toString());
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        JTypeSpecifier that = (JTypeSpecifier) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(generics, that.generics) &&
                Objects.equals(arrays, that.arrays);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, arrays);
    }

    public JName getName()
    {
        return name;
    }

    public Optional<List<JTypeSpecifier>> getGenerics()
    {
        return generics;
    }

    public List<JArray> getArrays()
    {
        return arrays;
    }
}
