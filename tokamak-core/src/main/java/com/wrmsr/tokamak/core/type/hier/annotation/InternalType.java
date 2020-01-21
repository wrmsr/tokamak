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

import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class InternalType
        implements TypeAnnotation
{
    public static final String NAME = "Internal";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, InternalType.class, TypeConstructor.of(InternalType::new));

    public InternalType()
    {
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
