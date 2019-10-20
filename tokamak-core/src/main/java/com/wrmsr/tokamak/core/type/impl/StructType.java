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
package com.wrmsr.tokamak.core.type.impl;

import com.wrmsr.tokamak.core.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class StructType
        extends KwargsType
{
    /*
    NOTE:
     - not 'record' to not clash with upcoming java keyword
    */

    private final Map<String, Type> members;

    public StructType(Map<String, Object> kwargs)
    {
        super("Struct", kwargs);
        members = immutableMapValues(this.kwargs, Type.class::cast);
    }

    public Map<String, Type> getMembers()
    {
        return members;
    }

    public Type getMember(String name)
    {
        return checkNotNull(members.get(checkNotEmpty(name)));
    }
}
