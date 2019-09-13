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
package com.wrmsr.tokamak.catalog;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class ConnectorType<T extends Connector>
{
    private final String name;
    private final Class<T> cls;

    public ConnectorType(String name, Class<T> cls)
    {
        this.name = checkNotEmpty(name);
        this.cls = checkNotNull(cls);
    }

    public String getName()
    {
        return name;
    }

    public Class<T> getCls()
    {
        return cls;
    }

    @Override
    public String toString()
    {
        return "ConnectorType{" +
                "name='" + name + '\'' +
                ", cls=" + cls +
                '}';
    }
}
