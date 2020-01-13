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

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class TypeRegistrant
        implements TypeConstructor
{
    private final String baseName;
    private final Class<? extends Type> cls;
    private final TypeConstructor constructor;

    public TypeRegistrant(
            String baseName,
            Class<? extends Type> cls,
            TypeConstructor constructor)
    {
        this.baseName = checkNotEmpty(baseName);
        this.cls = checkNotNull(cls);
        this.constructor = checkNotNull(constructor);
    }

    public String getBaseName()
    {
        return baseName;
    }

    public Class<? extends Type> getCls()
    {
        return cls;
    }

    public TypeConstructor getConstructor()
    {
        return constructor;
    }

    @Override
    public Type construct(List<Object> args, Map<String, Object> kwargs)
    {
        return constructor.construct(args, kwargs);
    }
}
