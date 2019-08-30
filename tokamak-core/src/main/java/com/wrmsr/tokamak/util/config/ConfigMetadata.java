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

package com.wrmsr.tokamak.util.config;

import com.wrmsr.tokamak.util.config.prop.Properties;
import com.wrmsr.tokamak.util.config.prop.Property;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfigMetadata
{
    private final Class<? extends Config> cls;

    private final Map<String, Property> properties;

    ConfigMetadata(Class<? extends Config> cls)
    {
        this.cls = checkNotNull(cls);

        properties = Properties.build(cls);
    }

    @Override
    public String toString()
    {
        return "ConfigMetadata{" +
                "cls=" + cls +
                '}';
    }

    public Class<? extends Config> getCls()
    {
        return cls;
    }

    public Map<String, Property> getProperties()
    {
        return properties;
    }
}
