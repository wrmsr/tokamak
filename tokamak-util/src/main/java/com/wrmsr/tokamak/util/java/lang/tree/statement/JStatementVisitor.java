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
package com.wrmsr.tokamak.util.java.lang.tree.statement;

import java.util.Objects;

public class JStatementVisitor<R, C>
{
    protected R process(JStatement jstatement, C context)
    {
        return jstatement.accept(this, context);
    }

    protected R visitStatement(JStatement jstatement, C context)
    {
        throw new IllegalStateException(Objects.toString(jstatement));
    }

    public R visitAnnotatedStatement(JAnnotatedStatement jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitBlank(JBlank jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitBlock(JBlock jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitBreak(JBreak jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitCase(JCase jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitContinue(JContinue jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitDoWhileLoop(JDoWhileLoop jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitEmpty(JEmpty jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitExpressionStatement(JExpressionStatement jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitForEach(JForEach jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitIf(JIf jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitLabeledStatement(JLabeledStatement jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitRawStatement(JRawStatement jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitReturn(JReturn jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitSwitch(JSwitch jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitThrow(JThrow jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitVariable(JVariable jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }

    public R visitWhileLoop(JWhileLoop jstatement, C context)
    {
        return visitStatement(jstatement, context);
    }
}
