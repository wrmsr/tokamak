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
package com.wrmsr.tokamak.core.type;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class FunctionType
        extends AbstractType
{
    private final Type returnType;
    private final Map<String, Type> paramTypes;

    public FunctionType(Type returnType, Map<String, Type> paramTypes)
    {
        super("Function");
        this.returnType = checkNotNull(returnType);
        this.paramTypes = ImmutableMap.copyOf(checkOrdered(paramTypes));
    }

    public Type getReturnType()
    {
        return returnType;
    }

    public Map<String, Type> getParamTypes()
    {
        return paramTypes;
    }

    @Override
    public java.lang.reflect.Type getReflect()
    {
        return null;
    }
}
