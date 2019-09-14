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

package com.wrmsr.tokamak.parser.tree;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.parser.tree.visitor.AstVisitor;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FunctionCallExpression
        extends Expression
{
    private final QualifiedName name;
    private final List<Expression> args;

    public FunctionCallExpression(QualifiedName name, List<Expression> args)
    {
        this.name = checkNotNull(name);
        this.args = ImmutableList.copyOf(args);
    }

    public QualifiedName getName()
    {
        return name;
    }

    public List<Expression> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitFunctionCallExpression(this, context);
    }
}
