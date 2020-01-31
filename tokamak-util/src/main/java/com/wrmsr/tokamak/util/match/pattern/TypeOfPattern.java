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
package com.wrmsr.tokamak.util.match.pattern;

import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeOfPattern<T>
        extends Pattern<T>
{
    private final Class<?> cls;

    public TypeOfPattern(Class<?> cls)
    {
        this.cls = checkNotNull(cls);
    }

    public Class<?> getCls()
    {
        return cls;
    }

    @Override
    public <R, C> R accept(PatternVisitor<R, C> visitor, C context)
    {
        return visitor.visitTypeOf(this, context);
    }
}
