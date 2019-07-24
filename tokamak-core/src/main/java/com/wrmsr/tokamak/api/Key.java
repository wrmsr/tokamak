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
package com.wrmsr.tokamak.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AllKey.class, name = "all"),
        @JsonSubTypes.Type(value = FieldKey.class, name = "field"),
        @JsonSubTypes.Type(value = IdKey.class, name = "id")
})
public interface Key
{
    static AllKey all()
    {
        return AllKey.INSTANCE;
    }

    static <V> FieldKey<V> of(FieldName field, V value)
    {
        return new FieldKey<>(field, value);
    }

    static IdKey of(Id id)
    {
        return new IdKey(id);
    }
}
