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
package com.wrmsr.tokamak.core.search2.search.node.visitor;

import com.wrmsr.tokamak.core.search2.search.node.SAnd;
import com.wrmsr.tokamak.core.search2.search.node.SCompare;
import com.wrmsr.tokamak.core.search2.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search2.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search2.search.node.SCurrent;
import com.wrmsr.tokamak.core.search2.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search2.search.node.SFlattenArray;
import com.wrmsr.tokamak.core.search2.search.node.SFlattenObject;
import com.wrmsr.tokamak.core.search2.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search2.search.node.SIndex;
import com.wrmsr.tokamak.core.search2.search.node.SInput;
import com.wrmsr.tokamak.core.search2.search.node.SJsonLiteral;
import com.wrmsr.tokamak.core.search2.search.node.SNegate;
import com.wrmsr.tokamak.core.search2.search.node.SNode;
import com.wrmsr.tokamak.core.search2.search.node.SOperator;
import com.wrmsr.tokamak.core.search2.search.node.SOr;
import com.wrmsr.tokamak.core.search2.search.node.SParameter;
import com.wrmsr.tokamak.core.search2.search.node.SProject;
import com.wrmsr.tokamak.core.search2.search.node.SProperty;
import com.wrmsr.tokamak.core.search2.search.node.SSelection;
import com.wrmsr.tokamak.core.search2.search.node.SSequence;
import com.wrmsr.tokamak.core.search2.search.node.SSlice;
import com.wrmsr.tokamak.core.search2.search.node.SString;

import java.util.Objects;

public abstract class SNodeVisitor<R, C>
{
    public R process(SNode node, C context)
    {
        return node.accept(this, context);
    }

    protected R visitNode(SNode node, C context)
    {
        throw new IllegalArgumentException(Objects.toString(node));
    }

    public R visitAnd(SAnd node, C context)
    {
        return visitOperator(node, context);
    }

    public R visitCompare(SCompare node, C context)
    {
        return visitOperator(node, context);
    }

    public R visitCreateArray(SCreateArray node, C context)
    {
        return visitNode(node, context);
    }

    public R visitCreateObject(SCreateObject node, C context)
    {
        return visitNode(node, context);
    }

    public R visitCurrent(SCurrent node, C context)
    {
        return visitNode(node, context);
    }

    public R visitExpressionRef(SExpressionRef node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFlattenArray(SFlattenArray node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFlattenObject(SFlattenObject node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFunctionCall(SFunctionCall node, C context)
    {
        return visitNode(node, context);
    }

    public R visitIndex(SIndex node, C context)
    {
        return visitNode(node, context);
    }

    public R visitInput(SInput node, C context)
    {
        return visitNode(node, context);
    }

    public R visitJsonLiteral(SJsonLiteral node, C context)
    {
        return visitNode(node, context);
    }

    public R visitNegate(SNegate node, C context)
    {
        return visitNode(node, context);
    }

    public R visitOperator(SOperator node, C context)
    {
        return visitNode(node, context);
    }

    public R visitOr(SOr node, C context)
    {
        return visitOperator(node, context);
    }

    public R visitParameter(SParameter node, C context)
    {
        return visitNode(node, context);
    }

    public R visitProject(SProject node, C context)
    {
        return visitNode(node, context);
    }

    public R visitProperty(SProperty node, C context)
    {
        return visitNode(node, context);
    }

    public R visitSelection(SSelection node, C context)
    {
        return visitNode(node, context);
    }

    public R visitSequence(SSequence node, C context)
    {
        return visitNode(node, context);
    }

    public R visitSlice(SSlice node, C context)
    {
        return visitNode(node, context);
    }

    public R visitString(SString node, C context)
    {
        return visitNode(node, context);
    }
}
