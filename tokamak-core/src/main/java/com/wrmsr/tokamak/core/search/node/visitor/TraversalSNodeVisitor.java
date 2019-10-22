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
package com.wrmsr.tokamak.core.search.node.visitor;

import com.wrmsr.tokamak.core.search.node.SAnd;
import com.wrmsr.tokamak.core.search.node.SCompare;
import com.wrmsr.tokamak.core.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search.node.SNegate;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;

public class TraversalSNodeVisitor<R, C>
        extends SNodeVisitor<R, C>
{
    protected C traverseContext(SNode node, C context)
    {
        return context;
    }

    @Override
    protected R visitNode(SNode node, C context)
    {
        return null;
    }

    @Override
    public R visitAnd(SAnd node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getLeft(), traversedContext);
        process(node.getRight(), traversedContext);

        return super.visitAnd(node, context);
    }

    @Override
    public R visitCompare(SCompare node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getLeft(), traversedContext);
        process(node.getRight(), traversedContext);

        return super.visitCompare(node, context);
    }

    @Override
    public R visitCreateArray(SCreateArray node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getItems().forEach(n -> process(n, traversedContext));

        return super.visitCreateArray(node, context);
    }

    @Override
    public R visitCreateObject(SCreateObject node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getFields().values().forEach(n -> process(n, traversedContext));

        return super.visitCreateObject(node, context);
    }

    @Override
    public R visitExpressionRef(SExpressionRef node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getExpression(), traversedContext);

        return super.visitExpressionRef(node, context);
    }

    @Override
    public R visitFunctionCall(SFunctionCall node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getArgs().forEach(n -> process(n, traversedContext));

        return super.visitFunctionCall(node, context);
    }

    @Override
    public R visitNegate(SNegate node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getItem(), traversedContext);

        return super.visitNegate(node, context);
    }

    @Override
    public R visitOr(SOr node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getLeft(), traversedContext);
        process(node.getRight(), traversedContext);

        return super.visitOr(node, context);
    }

    @Override
    public R visitProject(SProject node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getChild(), traversedContext);

        return super.visitProject(node, context);
    }

    @Override
    public R visitSelection(SSelection node, C context)
    {
        C traversedContext = traverseContext(node, context);
        process(node.getChild(), traversedContext);

        return super.visitSelection(node, context);
    }

    @Override
    public R visitSequence(SSequence node, C context)
    {
        C traversedContext = traverseContext(node, context);
        node.getItems().forEach(n -> process(n, traversedContext));

        return super.visitSequence(node, context);
    }
}
