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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.parse.TypeBaseVisitor;
import com.wrmsr.tokamak.core.parse.TypeLexer;
import com.wrmsr.tokamak.core.parse.TypeParser;
import com.wrmsr.tokamak.core.tree.CaseInsensitiveCharStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TypeParsing
{
    private TypeParsing()
    {
    }

    @Immutable
    public static final class RawParsedType
    {
        private final String name;
        private final List<ArgOrKwarg> items;

        public RawParsedType(String name, List<ArgOrKwarg> items)
        {
            this.name = checkNotNull(name);
            this.items = ImmutableList.copyOf(items);
        }

        @Override
        public String toString()
        {
            return "RawParsedType{" +
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

    @Immutable
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

    public static RawParsedType rawParseType(String str)
    {
        CharStream input = new CaseInsensitiveCharStream(CharStreams.fromString(str));
        TypeLexer lexer = new TypeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TypeParser parser = new TypeParser(tokens);

        return (RawParsedType) parser.type().accept(new TypeBaseVisitor<Object>()
        {
            @Override
            public Object visitType(TypeParser.TypeContext ctx)
            {
                List<ArgOrKwarg> items = ctx.argOrKwarg().stream().map(this::visit).map(ArgOrKwarg.class::cast).collect(toImmutableList());
                return new RawParsedType(ctx.NAME().getText(), items);
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

    @Immutable
    public static final class ParsedType
    {
        private final String name;
        private final List<Object> args;
        private final Map<String, Object> kwargs;

        public ParsedType(String name, List<Object> args, Map<String, Object> kwargs)
        {
            this.name = checkNotNull(name);
            this.args = ImmutableList.copyOf(args);
            this.kwargs = ImmutableMap.copyOf(kwargs);
        }

        @Override
        public String toString()
        {
            return "ParsedType{" +
                    "name='" + name + '\'' +
                    ", args=" + args +
                    ", kwargs=" + kwargs +
                    '}';
        }

        public String getName()
        {
            return name;
        }

        public List<Object> getArgs()
        {
            return args;
        }

        public Map<String, Object> getKwargs()
        {
            return kwargs;
        }
    }

    public static ParsedType cleanRawParsedType(RawParsedType rawParsedType)
    {
        ImmutableList.Builder<Object> args = ImmutableList.builder();
        ImmutableMap.Builder<String, Object> kwargs = ImmutableMap.builder();

        int i = 0;
        for (; i < rawParsedType.items.size(); ++i) {
            ArgOrKwarg item = rawParsedType.items.get(i);
            if (item.name.isPresent()) {
                break;
            }
            args.add(cleanRawItem(item.value));
        }
        for (; i < rawParsedType.items.size(); ++i) {
            ArgOrKwarg item = rawParsedType.items.get(i);
            checkArgument(item.name.isPresent());
            kwargs.put(item.name.get(), cleanRawItem(item.value));
        }

        return new ParsedType(rawParsedType.name, args.build(), kwargs.build());
    }

    public static Object cleanRawItem(Object rawItem)
    {
        if (rawItem instanceof RawParsedType) {
            return cleanRawParsedType((RawParsedType) rawItem);
        }
        else {
            return rawItem;
        }
    }

    public static ParsedType parseType(String str)
    {
        return cleanRawParsedType(rawParseType(str));
    }
}
