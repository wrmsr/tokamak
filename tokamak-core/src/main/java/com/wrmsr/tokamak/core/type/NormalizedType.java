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

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class NormalizedType
{
    private final Type item;

    private final Map<Class<? extends SigilType>, SigilType> sigilsByType;

    @SuppressWarnings({"unchecked"})
    public NormalizedType(Type type)
    {
        Type curType = checkNotNull(type);
        ImmutableMap.Builder<Class<? extends SigilType>, SigilType> sigilsByType = ImmutableMap.builder();

        while (curType instanceof SigilType) {
            SigilType sigilType = (SigilType) curType;
            sigilsByType.put((Class<? extends SigilType>) curType.getClass(), sigilType);
            curType = checkNotNull(sigilType.getItem());
        }

        this.item = checkNotNull(curType);
        this.sigilsByType = sigilsByType.build();
    }

    public Type getItem()
    {
        return item;
    }

    public Map<Class<? extends SigilType>, SigilType> getSigilsByType()
    {
        return sigilsByType;
    }
}
