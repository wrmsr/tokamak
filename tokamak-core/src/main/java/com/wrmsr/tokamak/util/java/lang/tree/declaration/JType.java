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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.java.lang.JAccess;
import com.wrmsr.tokamak.util.java.lang.tree.JInheritance;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JType
        extends JDeclaration
{
    public enum Kind
    {
        CLASS,
        INTERFACE,
        ENUM
    }

    private final Set<JAccess> access;
    private final Kind kind;
    private final String name;
    private final List<JInheritance> inheritances;
    private final List<JDeclaration> body;

    public JType(Set<JAccess> access, Kind kind, String name, List<JInheritance> inheritances, List<JDeclaration> body)
    {
        this.access = ImmutableSet.copyOf(access);
        this.kind = checkNotNull(kind);
        this.name = checkNotNull(name);
        this.inheritances = ImmutableList.copyOf(inheritances);
        this.body = ImmutableList.copyOf(body);
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
        JType jType = (JType) o;
        return Objects.equals(access, jType.access) &&
                kind == jType.kind &&
                Objects.equals(name, jType.name) &&
                Objects.equals(inheritances, jType.inheritances) &&
                Objects.equals(body, jType.body);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(access, kind, name, inheritances, body);
    }

    public Set<JAccess> getAccess()
    {
        return access;
    }

    public Kind getKind()
    {
        return kind;
    }

    public String getName()
    {
        return name;
    }

    public List<JInheritance> getInheritances()
    {
        return inheritances;
    }

    public List<JDeclaration> getBody()
    {
        return body;
    }

    @Override
    public <R, C> R accept(JDeclarationVisitor<R, C> visitor, C context)
    {
        return visitor.visitJType(this, context);
    }
}
