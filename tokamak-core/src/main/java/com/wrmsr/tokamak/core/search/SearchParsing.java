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

import com.wrmsr.tokamak.core.parse.SearchBaseVisitor;
import com.wrmsr.tokamak.core.parse.SearchLexer;
import com.wrmsr.tokamak.core.parse.SearchParser;
import com.wrmsr.tokamak.core.search.node.SNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class SearchParsing
{
    private SearchParsing()
    {
    }

    public static SearchParser parse(String str)
    {
        CharStream input = CharStreams.fromString(str);
        SearchLexer lexer = new SearchLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new SearchParser(tokens);
    }

    public static SNode build(ParseTree tree)
    {
        return tree.accept(new SearchBaseVisitor<SNode>()
        {
            @Override
            protected SNode defaultResult()
            {
                return super.defaultResult();
            }

            @Override
            protected SNode aggregateResult(SNode aggregate, SNode nextResult)
            {
                checkState(aggregate == null);
                return checkNotNull(nextResult);
            }

            private <T> List<T> visit(List<? extends ParserRuleContext> contexts, Class<T> cls)
            {
                return contexts.stream()
                        .map(this::visit)
                        .map(cls::cast)
                        .collect(toImmutableList());
            }
        });
    }
}
