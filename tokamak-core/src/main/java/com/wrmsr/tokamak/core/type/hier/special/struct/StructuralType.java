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
package com.wrmsr.tokamak.core.type.hier.special.struct;

import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.TypeUtils;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

@Immutable
public final class StructuralType
        extends AbstractStructType
{
    public static final String NAME = "Structural";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, StructuralType.class, TypeConstructor.of(
            (Map<String, Object> kwargs) -> new StructuralType(TypeUtils.objectsToTypes(kwargs))));

    public StructuralType(Map<String, Type> members)
    {
        super(members);
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
