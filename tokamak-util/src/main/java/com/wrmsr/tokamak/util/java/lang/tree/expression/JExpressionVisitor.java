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
package com.wrmsr.tokamak.util.java.lang.tree.expression;

import java.util.Objects;

public class JExpressionVisitor<R, C>
{
    protected R visitExpression(JExpression expression, C context)
    {
        throw new IllegalStateException(Objects.toString(expression));
    }

    public R visitArrayAccess(JArrayAccess expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitAssignment(JAssignment expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitCast(JCast expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitBinary(JBinary expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitConditional(JConditional expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitIdent(JIdent expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitLambda(JLambda expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitLiteral(JLiteral expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitLongArrayLiteral(JLongArrayLiteral expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitLongStringLiteral(JLongStringLiteral expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitMemberAccess(JMemberAccess expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitMethodInvocation(JMethodInvocation expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitMethodReference(JMethodReference expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitNew(JNew expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitNewArray(JNewArray expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitRawExpression(JRawExpression expression, C context)
    {
        return visitExpression(expression, context);
    }

    public R visitUnary(JUnary expression, C context)
    {
        return visitExpression(expression, context);
    }
}
