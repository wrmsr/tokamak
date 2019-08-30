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

import com.wrmsr.tokamak.util.config.Config;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public abstract class Property
{
    private String name;

    Property(String name)
    {
        this.name = checkNotEmpty(name);
    }

    public String getName()
    {
        return name;
    }

    public abstract Object get(Config cfg);

    public abstract void set(Config cfg, Object value);
}
