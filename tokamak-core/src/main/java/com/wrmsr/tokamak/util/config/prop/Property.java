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
package com.wrmsr.tokamak.util.config.prop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wrmsr.tokamak.util.config.Config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public abstract class Property
{
    private String name;
    private TypeReference type;

    Property(String name, TypeReference type)
    {
        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);
    }

    public String getName()
    {
        return name;
    }

    public TypeReference getType()
    {
        return type;
    }

    public abstract Object get(Config cfg);

    public abstract void set(Config cfg, Object value);
}
