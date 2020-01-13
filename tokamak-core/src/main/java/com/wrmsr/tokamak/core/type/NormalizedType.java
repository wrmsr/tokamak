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

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

@Immutable
public final class NormalizedType
{
    private final Type item;

    private final Map<Class<? extends Type.Sigil>, Type.Sigil> sigilsByCls;
    private final Map<String, Type.Sigil> sigilsByName;

    private final List<Object> args;
    private final Map<String, Object> kwargs;

    public NormalizedType(Type type)
    {
        Type curType = checkNotNull(type);
        ImmutableMap.Builder<Class<? extends Type.Sigil>, Type.Sigil> sigilsByCls = ImmutableMap.builder();
        ImmutableMap.Builder<String, Type.Sigil> sigilsByBaseName = ImmutableMap.builder();
        while (curType instanceof Type.Sigil) {
            Type.Sigil sigilType = (Type.Sigil) curType;
            sigilsByCls.put(sigilType.getClass(), sigilType);
            sigilsByBaseName.put(sigilType.getBaseName(), sigilType);
            curType = checkNotNull(sigilType.getItem());
        }
        this.item = checkNotNull(curType);
        this.sigilsByCls = sigilsByCls.build();
        this.sigilsByName = sigilsByBaseName.build();

        args = immutableMapItems(item.getArgs(), NormalizedType::of);
        kwargs = immutableMapValues(item.getKwargs(), NormalizedType::of);
    }

    public Type getItem()
    {
        return item;
    }

    public Map<Class<? extends Type.Sigil>, Type.Sigil> getSigilsByCls()
    {
        return sigilsByCls;
    }

    public Map<String, Type.Sigil> getSigilsByName()
    {
        return sigilsByName;
    }

    public List<Object> getArgs()
    {
        return args;
    }

    public Map<String, Object> getKwargs()
    {
        return kwargs;
    }

    public static Object of(Object item)
    {
        if (item instanceof Type) {
            return new NormalizedType((Type) item);
        }
        else {
            return item;
        }
    }
}
