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
package com.wrmsr.tokamak.util.kv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class MapKv<K, V>
        implements Kv<K, V>
{
    private final Map<K, V> map;

    @JsonCreator
    public MapKv(
            @JsonProperty("map") Map<K, V> map)
    {
        this.map = checkNotNull(map);
    }

    @JsonProperty("map")
    public Map<K, V> getMap()
    {
        return map;
    }

    @Override
    public V get(K key)
    {
        return map.get(key);
    }

    @Override
    public boolean containsKey(K key)
    {
        return map.containsKey(key);
    }

    @Override
    public void put(K key, V value)
    {
        map.put(key, value);
    }

    @Override
    public void remove(K key)
    {
        map.remove(key);
    }

    @Override
    public Iterator<K> iterator()
    {
        return map.keySet().iterator();
    }
}
