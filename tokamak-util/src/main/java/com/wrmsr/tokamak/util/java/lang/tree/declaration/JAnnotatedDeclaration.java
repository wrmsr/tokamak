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

import com.wrmsr.tokamak.util.java.lang.JName;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpression;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JAnnotatedDeclaration
        extends JDeclaration
{
    private final JName annotation;
    private final Optional<List<JExpression>> args;
    private final JDeclaration declaration;

    public JAnnotatedDeclaration(JName annotation, Optional<List<JExpression>> args, JDeclaration declaration)
    {
        this.annotation = checkNotNull(annotation);
        this.args = checkNotNull(args);
        this.declaration = checkNotNull(declaration);
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
        JAnnotatedDeclaration that = (JAnnotatedDeclaration) o;
        return Objects.equals(annotation, that.annotation) &&
                Objects.equals(args, that.args) &&
                Objects.equals(declaration, that.declaration);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(annotation, args, declaration);
    }

    public JName getAnnotation()
    {
        return annotation;
    }

    public Optional<List<JExpression>> getArgs()
    {
        return args;
    }

    public JDeclaration getDeclaration()
    {
        return declaration;
    }

    @Override
    public <R, C> R accept(JDeclarationVisitor<R, C> visitor, C context)
    {
        return visitor.visitAnnotatedDeclaration(this, context);
    }
}
