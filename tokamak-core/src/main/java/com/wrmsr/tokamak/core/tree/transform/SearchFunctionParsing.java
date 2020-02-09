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
package com.wrmsr.tokamak.core.tree.transform;

import com.wrmsr.tokamak.core.search.SearchParsing;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.transform.STransforms;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TSearch;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class SearchFunctionParsing
{
    private SearchFunctionParsing()
    {
    }

    public static TNode parseSearches(TNode node)
    {
        return node.accept(new TNodeRewriter<Void>()
        {
            @Override
            public TNode visitFunctionCallExpression(TFunctionCallExpression node, Void context)
            {
                if (node.getName().equals("search")) {
                    // FIXME: SParameters
                    TNode arg = checkSingle(node.getArgs());
                    checkState(arg instanceof TStringLiteral);
                    String src = ((TStringLiteral) arg).getValue().getValue();
                    SNode search = SearchParsing.build(SearchParsing.parse(src).singleExpression());
                    search = STransforms.inlineSequences(search);
                    return new TSearch(search);
                }
                else {
                    return super.visitFunctionCallExpression(node, context);
                }
            }
        }, null);
    }
}
