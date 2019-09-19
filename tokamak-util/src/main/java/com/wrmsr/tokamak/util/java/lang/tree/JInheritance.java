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
package com.wrmsr.tokamak.util.java.lang.tree;

import com.wrmsr.tokamak.util.java.lang.JName;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JInheritance
{
    public enum Kind
    {
        EXTENDS,
        IMPLEMENTS
    }

    private final Kind kind;
    private final JName name;

    public JInheritance(Kind kind, JName name)
    {
        this.kind = checkNotNull(kind);
        this.name = checkNotNull(name);
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
        JInheritance that = (JInheritance) o;
        return kind == that.kind &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(kind, name);
    }

    public Kind getKind()
    {
        return kind;
    }

    public JName getName()
    {
        return name;
    }
}
