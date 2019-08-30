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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class Properties
{
    /*
    TODO:
     - name overrides
     - toString
    */

    private Properties()
    {
    }

    public static Map<String, Property> build(Class<? extends Config> cls)
    {
        Map<String, Property> properties = new LinkedHashMap<>();
        Map<String, Method> getters = new LinkedHashMap<>();

        for (Class<?> cur = cls; (cur != null) && !cur.equals(Object.class); cur = cur.getSuperclass()) {
            for (Method method : cur.getDeclaredMethods()) {
                if (method.isAnnotationPresent(com.wrmsr.tokamak.util.config.ConfigProperty.class)) {
                    String methodName = method.getName();
                    if (methodName.startsWith("set") || methodName.startsWith("get") || methodName.startsWith("is")) {
                        boolean isGetter = !methodName.startsWith("set");
                        String name = methodName.substring(methodName.startsWith("is") ? 2 : 3);
                        checkArgument(Character.isUpperCase(name.charAt(0)));
                        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                        if (isGetter) {
                            checkState(!getters.containsKey(name));
                            getters.put(name, method);
                        }
                        else {
                            checkState(!properties.containsKey(name));
                            properties.put(name, new BeanProperty(name, method, Optional.empty()));
                        }
                    }
                }
            }

            for (Field field : cur.getDeclaredFields()) {
                if (field.isAnnotationPresent(com.wrmsr.tokamak.util.config.ConfigProperty.class)) {
                    String name = field.getName();
                    checkState(!properties.containsKey(name));
                    properties.put(name, new FieldProperty(name, field));
                }
            }
        }

        for (Map.Entry<String, Method> e : getters.entrySet()) {
            BeanProperty p = (BeanProperty) checkNotNull(properties.get(e.getKey()));
            checkState(!p.getGetter().isPresent());
            properties.put(e.getKey(), new BeanProperty(p.getName(), p.getSetter(), Optional.of(e.getValue())));
        }

        return ImmutableMap.copyOf(properties);
    }
}
