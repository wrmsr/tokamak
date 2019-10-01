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
package com.wrmsr.tokamak.core.type;

import com.wrmsr.tokamak.core.parse.TypeBaseVisitor;
import com.wrmsr.tokamak.core.parse.TypeLexer;
import com.wrmsr.tokamak.core.parse.TypeParser;
import com.wrmsr.tokamak.core.tree.CaseInsensitiveCharStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TypeParsing
{
    private TypeParsing()
    {
    }

    public static final class ParsedType
    {
        private final String name;
        private final List<ArgOrKwarg> items;

        public ParsedType(String name, List<ArgOrKwarg> items)
        {
            this.name = name;
            this.items = items;
        }

        @Override
        public String toString()
        {
            return "ParsedType{" +
                    "name='" + name + '\'' +
                    ", items=" + items +
                    '}';
        }

        public String getName()
        {
            return name;
        }

        public List<ArgOrKwarg> getItems()
        {
            return items;
        }
    }

    public static final class ArgOrKwarg
    {
        private final Optional<String> name;
        private final Object value;

        public ArgOrKwarg(Optional<String> name, Object value)
        {
            this.name = checkNotNull(name);
            this.value = checkNotNull(value);
        }

        @Override
        public String toString()
        {
            return "ArgOrKwarg{" +
                    "name=" + name +
                    ", value=" + value +
                    '}';
        }

        public Optional<String> getName()
        {
            return name;
        }

        public Object getValue()
        {
            return value;
        }
    }

    public static ParsedType parseType(String str)
    {
        CharStream input = new CaseInsensitiveCharStream(CharStreams.fromString(str));
        TypeLexer lexer = new TypeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TypeParser parser = new TypeParser(tokens);

        return (ParsedType) parser.type().accept(new TypeBaseVisitor<Object>()
        {
            @Override
            public Object visitType(TypeParser.TypeContext ctx)
            {
                List<ArgOrKwarg> items = ctx.argOrKwarg().stream().map(this::visit).map(ArgOrKwarg.class::cast).collect(toImmutableList());
                return new ParsedType(ctx.NAME().getText(), items);
            }

            @Override
            public Object visitArg(TypeParser.ArgContext ctx)
            {
                return new ArgOrKwarg(Optional.empty(), visit(ctx.typeOrInt()));
            }

            @Override
            public Object visitKwarg(TypeParser.KwargContext ctx)
            {
                return new ArgOrKwarg(Optional.of(ctx.NAME().getText()), visit(ctx.typeOrInt()));
            }

            @Override
            public Object visitTypeOrInt(TypeParser.TypeOrIntContext ctx)
            {
                if (ctx.INT() != null) {
                    return Long.parseLong(ctx.INT().getText());
                }
                else if (ctx.type() != null) {
                    return visit(ctx.type());
                }
                else {
                    throw new IllegalStateException();
                }
            }
        });
    }
}
