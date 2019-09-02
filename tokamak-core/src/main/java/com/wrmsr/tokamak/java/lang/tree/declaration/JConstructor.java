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
package com.wrmsr.tokamak.java.lang.tree.declaration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JArg;
import com.wrmsr.tokamak.java.lang.tree.statement.JBlock;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JConstructor
        extends JDeclaration
{
    private final Set<JAccess> access;
    private final String name;
    private final List<JArg> args;
    private final JBlock body;

    public JConstructor(Set<JAccess> access, String name, List<JArg> args, JBlock body)
    {
        this.access = ImmutableSet.copyOf(access);
        this.name = checkNotNull(name);
        this.args = ImmutableList.copyOf(args);
        this.body = checkNotNull(body);
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
        JConstructor that = (JConstructor) o;
        return Objects.equals(access, that.access) &&
                Objects.equals(name, that.name) &&
                Objects.equals(args, that.args) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(access, name, args, body);
    }

    public Set<JAccess> getAccess()
    {
        return access;
    }

    public String getName()
    {
        return name;
    }

    public List<JArg> getArgs()
    {
        return args;
    }

    public JBlock getBody()
    {
        return body;
    }

    @Override
    public <R, C> R accept(JDeclarationVisitor<R, C> visitor, C context)
    {
        return visitor.visitJConstructor(this, context);
    }
}
