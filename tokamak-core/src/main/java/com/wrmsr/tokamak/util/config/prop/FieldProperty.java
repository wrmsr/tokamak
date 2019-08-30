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

import java.lang.reflect.Field;

public final class FieldProperty
        extends Property
{
    private final Field field;

    FieldProperty(String name, Field field)
    {
        super(name);
        this.field = field;
    }

    @Override
    public Object get(Config cfg)
    {
        try {
            return field.get(cfg);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Config cfg, Object value)
    {
        try {
            field.set(cfg, value);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
