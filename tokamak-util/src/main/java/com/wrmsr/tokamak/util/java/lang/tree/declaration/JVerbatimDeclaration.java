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

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JVerbatimDeclaration
        extends JDeclaration
{
    private final String text;

    public JVerbatimDeclaration(String text)
    {
        this.text = checkNotNull(text);
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
        JVerbatimDeclaration that = (JVerbatimDeclaration) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(text);
    }

    public String getText()
    {
        return text;
    }

    @Override
    public <R, C> R accept(JDeclarationVisitor<R, C> visitor, C context)
    {
        return visitor.visitVerbatimDeclaration(this, context);
    }
}
