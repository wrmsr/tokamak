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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.config.Config;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public final class Properties
{
    private Properties()
    {
    }

    public static Map<String, Property> build(Class<? extends Config> cls)
    {
        ImmutableMap.Builder<String, Property> map = ImmutableMap.builder();
        for (Class<?> cur = cls; (cur != null) && !cur.equals(Object.class); cur = cur.getSuperclass()) {
            for (Method method : cur.getDeclaredMethods()) {
                if (method.isAnnotationPresent(com.wrmsr.tokamak.util.config.ConfigProperty.class)) {
                    checkArgument(method.getName().startsWith("set"));
                    String name = method.getName().substring(3);
                    checkArgument(Character.isUpperCase(name.charAt(0)));
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    map.put(name, new BeanProperty(name, method, Optional.empty()));
                }
            }
            for (java.lang.reflect.Field field : cur.getDeclaredFields()) {
                if (field.isAnnotationPresent(com.wrmsr.tokamak.util.config.ConfigProperty.class)) {
                    String name = field.getName();
                    map.put(name, new FieldProperty(name, field));
                }
            }
        }
        return map.build();
    }
}
