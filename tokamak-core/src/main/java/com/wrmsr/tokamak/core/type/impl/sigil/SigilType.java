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
package com.wrmsr.tokamak.core.type.impl.sigil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.impl.AbstractType;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class SigilType
        extends AbstractType
        implements Type.Sigil
{
    public SigilType(
            String name,
            Type itemType,
            List<Object> args,
            ImmutableMap<String, Object> kwargs)
    {
        super(name, OptionalInt.empty(), ImmutableList.builder().add(itemType).addAll(args).build(), ImmutableMap.copyOf(kwargs));
        getArgs().subList(1, getArgs().size()).forEach(i -> checkArgument(!(i instanceof Type)));
        getKwargs().values().forEach(i -> checkArgument(!(i instanceof Type)));
    }

    public SigilType(String name, Type itemType)
    {
        this(name, itemType, ImmutableList.of(), ImmutableMap.of());
    }

    public Type getItem()
    {
        return (Type) checkNotNull(getArgs().get(0));
    }
}
