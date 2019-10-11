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
import com.wrmsr.tokamak.core.search.node.SComparison;
import com.wrmsr.tokamak.core.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search.node.SCurrent;
import com.wrmsr.tokamak.core.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search.node.SFlattenArray;
import com.wrmsr.tokamak.core.search.node.SFlattenObject;
import com.wrmsr.tokamak.core.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search.node.SIndex;
import com.wrmsr.tokamak.core.search.node.SJsonLiteral;
import com.wrmsr.tokamak.core.search.node.SNegate;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;

public class TraversalSNodeVisitor<R, C>
        extends SNodeVisitor<R, C>
{
    @Override
    protected R visitNode(SNode node, C context)
    {
        return super.visitNode(node, context);
    }

    @Override
    public R visitAnd(SAnd node, C context)
    {
        process(node.getLeft(), context);
        process(node.getRight(), context);

        return super.visitAnd(node, context);
    }

    @Override
    public R visitComparison(SComparison node, C context)
    {
        process(node.getLeft(), context);
        process(node.getRight(), context);

        return super.visitComparison(node, context);
    }

    @Override
    public R visitCreateArray(SCreateArray node, C context)
    {
        node.getItems().forEach(n -> process(n, context));

        return super.visitCreateArray(node, context);
    }

    @Override
    public R visitCreateObject(SCreateObject node, C context)
    {
        node.getFields().values().forEach(n -> process(n, context));

        return super.visitCreateObject(node, context);
    }

    @Override
    public R visitExpressionRef(SExpressionRef node, C context)
    {
        process(node.getExpression(), context);

        return super.visitExpressionRef(node, context);
    }

    @Override
    public R visitFunctionCall(SFunctionCall node, C context)
    {
        node.getArgs().forEach(n -> process(n, context));

        return super.visitFunctionCall(node, context);
    }

    @Override
    public R visitNegate(SNegate node, C context)
    {
        process(node.getItem(), context);

        return super.visitNegate(node, context);
    }

    @Override
    public R visitOr(SOr node, C context)
    {
        process(node.getLeft(), context);
        process(node.getRight(), context);

        return super.visitOr(node, context);
    }

    @Override
    public R visitProject(SProject node, C context)
    {
        process(node.getChild(), context);

        return super.visitProject(node, context);
    }

    @Override
    public R visitSelection(SSelection node, C context)
    {
        process(node.getChild(), context);

        return super.visitSelection(node, context);
    }

    @Override
    public R visitSequence(SSequence node, C context)
    {
        node.getItems().forEach(n -> process(n, context));

        return super.visitSequence(node, context);
    }
}
