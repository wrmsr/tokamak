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
package com.wrmsr.tokamak.util.java.lang.tree.declaration;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.java.lang.JAccess;
import com.wrmsr.tokamak.util.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpression;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JField
        extends JDeclaration
{
    private final Set<JAccess> access;
    private final JTypeSpecifier type;
    private final String name;
    private final Optional<JExpression> value;

    public JField(Set<JAccess> access, JTypeSpecifier type, String name, Optional<JExpression> value)
    {
        this.access = ImmutableSet.copyOf(access);
        this.type = checkNotNull(type);
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JField jField = (JField) o;
        return Objects.equals(access, jField.access) &&
                Objects.equals(type, jField.type) &&
                Objects.equals(name, jField.name) &&
                Objects.equals(value, jField.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(access, type, name, value);
    }

    public Set<JAccess> getAccess()
    {
        return access;
    }

    public JTypeSpecifier getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public Optional<JExpression> getValue()
    {
        return value;
    }

    @Override
    public <R, C> R accept(JDeclarationVisitor<R, C> visitor, C context)
    {
        return visitor.visitField(this, context);
    }
}
