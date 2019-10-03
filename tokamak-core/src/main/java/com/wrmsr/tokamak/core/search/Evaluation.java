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

package com.wrmsr.tokamak.core.search;

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
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.SSlice;
import com.wrmsr.tokamak.core.search.node.SString;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;

public final class Evaluation
{
    private Evaluation()
    {
    }

    public static void evaluate(SNode search, Object object)
    {
        search.accept(new SNodeVisitor<Object, Object>()
        {
            @Override
            public Object visitAnd(SAnd node, Object context)
            {
                return super.visitAnd(node, context);
            }

            @Override
            public Object visitComparison(SComparison node, Object context)
            {
                return super.visitComparison(node, context);
            }

            @Override
            public Object visitCreateArray(SCreateArray node, Object context)
            {
                return super.visitCreateArray(node, context);
            }

            @Override
            public Object visitCreateObject(SCreateObject node, Object context)
            {
                return super.visitCreateObject(node, context);
            }

            @Override
            public Object visitCurrent(SCurrent node, Object context)
            {
                return super.visitCurrent(node, context);
            }

            @Override
            public Object visitExpressionRef(SExpressionRef node, Object context)
            {
                return super.visitExpressionRef(node, context);
            }

            @Override
            public Object visitFlattenArray(SFlattenArray node, Object context)
            {
                return super.visitFlattenArray(node, context);
            }

            @Override
            public Object visitFlattenObject(SFlattenObject node, Object context)
            {
                return super.visitFlattenObject(node, context);
            }

            @Override
            public Object visitFunctionCall(SFunctionCall node, Object context)
            {
                return super.visitFunctionCall(node, context);
            }

            @Override
            public Object visitIndex(SIndex node, Object context)
            {
                return super.visitIndex(node, context);
            }

            @Override
            public Object visitJsonLiteral(SJsonLiteral node, Object context)
            {
                return super.visitJsonLiteral(node, context);
            }

            @Override
            public Object visitNegate(SNegate node, Object context)
            {
                return super.visitNegate(node, context);
            }

            @Override
            public Object visitOr(SOr node, Object context)
            {
                return super.visitOr(node, context);
            }

            @Override
            public Object visitProject(SProject node, Object context)
            {
                return super.visitProject(node, context);
            }

            @Override
            public Object visitProperty(SProperty node, Object context)
            {
                return super.visitProperty(node, context);
            }

            @Override
            public Object visitSelection(SSelection node, Object context)
            {
                return super.visitSelection(node, context);
            }

            @Override
            public Object visitSequence(SSequence node, Object context)
            {
                return super.visitSequence(node, context);
            }

            @Override
            public Object visitSlice(SSlice node, Object context)
            {
                return super.visitSlice(node, context);
            }

            @Override
            public Object visitString(SString node, Object context)
            {
                return super.visitString(node, context);
            }
        }, object);
    }
}
