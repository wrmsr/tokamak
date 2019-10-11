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

import java.util.Objects;

public class JDeclarationVisitor<R, C>
{
    protected R process(JDeclaration jdeclaration, C context)
    {
        return jdeclaration.accept(this, context);
    }

    protected R visitDeclaration(JDeclaration jdeclaration, C context)
    {
        throw new IllegalStateException(Objects.toString(jdeclaration));
    }

    public R visitAnnotatedDeclaration(JAnnotatedDeclaration jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitConstructor(JConstructor jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitDeclarationBlock(JDeclarationBlock jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitField(JField jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitInitializationBlock(JInitializationBlock jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitMethod(JMethod jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitRawDeclaration(JRawDeclaration jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitType(JType jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }

    public R visitVerbatimDeclaration(JVerbatimDeclaration jdeclaration, C context)
    {
        return visitDeclaration(jdeclaration, context);
    }
}
