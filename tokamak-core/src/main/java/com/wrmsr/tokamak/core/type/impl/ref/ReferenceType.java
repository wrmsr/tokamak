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
package com.wrmsr.tokamak.core.type.impl.ref;

import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.impl.AbstractType;
import com.wrmsr.tokamak.util.Cell;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class ReferenceType
        extends AbstractType
{
    private final Cell<Type> targetType = Cell.setOnce();

    public ReferenceType(String name)
    {
        super(name);
    }

    public ReferenceType(String name, Type targetType)
    {
        super(name);

        this.targetType.set(checkNotNull(targetType));
    }

    public Optional<Type> getTarget()
    {
        return targetType.getOptional();
    }

    void setTarget(Type targetType)
    {
        if (!this.targetType.isSet()) {
            this.targetType.set(checkNotNull(targetType));
        }
        else {
            checkArgument(this.targetType.get() == targetType);
        }
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return targetType.get().toReflect();
    }
}
