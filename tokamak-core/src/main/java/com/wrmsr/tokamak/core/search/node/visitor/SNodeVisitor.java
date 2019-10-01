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
import com.wrmsr.tokamak.core.search.node.SOperator;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.SSlice;
import com.wrmsr.tokamak.core.search.node.SString;

import java.util.Objects;

public abstract class SNodeVisitor<R, C>
{
    protected R visitNode(SNode snode, C context)
    {
        throw new IllegalArgumentException(Objects.toString(snode));
    }

    public R visitAnd(SAnd snode, C context)
    {
        return visitOperator(snode, context);
    }

    public R visitComparison(SComparison snode, C context)
    {
        return visitOperator(snode, context);
    }

    public R visitCreateArray(SCreateArray snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitCreateObject(SCreateObject snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitCurrent(SCurrent snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitExpressionRef(SExpressionRef snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitFlattenArray(SFlattenArray snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitFlattenObject(SFlattenObject snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitFunctionCall(SFunctionCall snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitIndex(SIndex snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitJsonLiteral(SJsonLiteral snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitNegate(SNegate snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitOperator(SOperator snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitOr(SOr snode, C context)
    {
        return visitOperator(snode, context);
    }

    public R visitProject(SProject snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitProperty(SProperty snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitSelection(SSelection snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitSequence(SSequence snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitSlice(SSlice snode, C context)
    {
        return visitNode(snode, context);
    }

    public R visitString(SString snode, C context)
    {
        return visitNode(snode, context);
    }
}
