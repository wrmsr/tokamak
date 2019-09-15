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

import java.util.Map;
import java.util.WeakHashMap;

public final class Configs
{
    /*
    TODO:
     - Optional/null handling (default non-optional child configs?)
     - json
      - ConfigContext?
     - guice
     - toString / hashCode / equals
     - defaults, validates, multi-field validators, compute defaults/injection/toposort
     - _im_mutability
     - mutability
      - jit subclasses blocking setters
      - dynamic sources (ala archaius) - zk + jdbccoord
     - field getter + explicit setter
     - abstracts / non-interface-configs
     - maven compilation step (runner not plugin)
     - class ConfigRegistry
     - varhandle codeless impl? possible?
      - even shittier HashMap impl possible?
     - inheritance
    */

    private Configs()
    {
    }

    private static final Object lock = new Object();
    private static final Map<Class<? extends Config>, ConfigMetadata> CACHE = new WeakHashMap<>();

    public static <T extends Config> ConfigMetadata getMetadata(Class<T> cls)
    {
        synchronized (lock) {
            return CACHE.computeIfAbsent(cls, ConfigMetadata::new);
        }
    }
}
