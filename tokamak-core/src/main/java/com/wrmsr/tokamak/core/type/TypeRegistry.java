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

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeRegistry
{
    public Type fromRepr(String str)
    {
        throw new IllegalStateException();
    }

    public String toRepr(Type type)
    {
        throw new IllegalStateException();
    }

    public Type fromReflect(java.lang.reflect.Type rfl)
    {
        throw new IllegalStateException();
    }

    public boolean areEquivalent(Type l, Type r)
    {
        checkNotNull(l);
        checkNotNull(r);
        if (l == Types.UNKNOWN || r == Types.UNKNOWN) {
            return true;
        }
        // FIXME: structural, nominal, lolinal
        return l.toSpec().equals(r.toSpec());
    }
}
