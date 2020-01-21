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
package com.wrmsr.tokamak.core.type.hier.annotation;

import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class EphemeralType
        implements TypeAnnotation
{
    public static final String NAME = "Ephemeral";
    public static final InternalType INSTANCE = new InternalType();
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, EphemeralType.class, TypeConstructor.of(INSTANCE));

    public EphemeralType()
    {
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
