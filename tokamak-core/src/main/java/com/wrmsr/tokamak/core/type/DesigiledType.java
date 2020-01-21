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
import com.wrmsr.tokamak.core.util.DerivableClassUniqueCollection;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

@Immutable
public final class DesigiledType
    implements DerivableClassUniqueCollection<Type.Sigil, DesigiledType>
{
    private final Type item;

    private final Map<Class<? extends Type.Sigil>, Type.Sigil> byCls;
    private final Map<String, Type.Sigil> byName;

    private final List<Object> args;
    private final Map<String, Object> kwargs;

    public DesigiledType(Type type)
    {
        Type curType = checkNotNull(type);
        ImmutableMap.Builder<Class<? extends Type.Sigil>, Type.Sigil> byCls = ImmutableMap.builder();
        ImmutableMap.Builder<String, Type.Sigil> byBaseName = ImmutableMap.builder();
        while (curType instanceof Type.Sigil) {
            Type.Sigil sigilType = (Type.Sigil) curType;
            byCls.put(sigilType.getClass(), sigilType);
            byBaseName.put(sigilType.getBaseName(), sigilType);
            curType = checkNotNull(sigilType.getItem());
        }
        this.item = checkNotNull(curType);
        this.byCls = byCls.build();
        this.byName = byBaseName.build();

        args = immutableMapItems(item.getArgs(), DesigiledType::desigil);
        kwargs = immutableMapValues(item.getKwargs(), DesigiledType::desigil);
    }

    public Type getItem()
    {
        return item;
    }

    public Map<Class<? extends Type.Sigil>, Type.Sigil> getByCls()
    {
        return byCls;
    }

    public Map<String, Type.Sigil> getByName()
    {
        return byName;
    }

    public List<Object> getArgs()
    {
        return args;
    }

    public Map<String, Object> getKwargs()
    {
        return kwargs;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Type.Sigil> Optional<T> get(Class<T> cls)
    {
        return Optional.ofNullable((T) byCls.get(cls));
    }

    public boolean contains(Class<? extends Type.Sigil> cls)
    {
        return byCls.containsKey(cls);
    }

    @Override
    public DesigiledType appended(Type.Sigil... items)
    {
        return null;
    }

    @Override
    public DesigiledType dropped(Class<? extends Type.Sigil>... clss)
    {
        return null;
    }

    @Override
    public DesigiledType updated(Type.Sigil... items)
    {
        return null;
    }

    public static Object desigil(Object item)
    {
        if (item instanceof Type) {
            return ((Type) item).desigil();
        }
        else {
            return item;
        }
    }
}
