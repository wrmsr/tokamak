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
package com.wrmsr.tokamak.codegen.write;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class CodeBlock
{
    /**
     * A heterogeneous list containing string literals and value placeholders.
     */
    final List<String> formatParts;
    final List<Object> args;

    private CodeBlock(Builder builder)
    {
        this.formatParts = ImmutableList.copyOf(builder.formatParts);
        this.args = ImmutableList.copyOf(builder.args);
    }

    public static CodeBlock of(String format, Object... args)
    {
        return new Builder().add(format, args).build();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public boolean isEmpty()
    {
        return formatParts.isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        StringWriter out = new StringWriter();
        try {
            CodeWriter.builder(out).build().emit(this);
            return out.toString();
        }
        catch (IOException e) {
            throw new AssertionError();
        }
    }

    public Builder toBuilder()
    {
        Builder builder = new Builder();
        builder.formatParts.addAll(formatParts);
        builder.args.addAll(args);
        return builder;
    }

    public static final class Builder
    {
        final List<String> formatParts = new ArrayList<>();
        final List<Object> args = new ArrayList<>();

        private Builder()
        {
        }

        public Builder add(String format, Object... args)
        {
            boolean hasRelative = false;
            boolean hasIndexed = false;
            int relativeParameterCount = 0;
            int[] indexedParameterCount = new int[args.length];

            for (int p = 0; p < format.length(); ) {
                if (format.charAt(p) != '$') {
                    int nextP = format.indexOf('$', p + 1);
                    if (nextP == -1) {
                        nextP = format.length();
                    }
                    formatParts.add(format.substring(p, nextP));
                    p = nextP;
                    continue;
                }

                p++; // '$'.

                // Consume zero or more digits, leaving 'c' as the first non-digit char after the '$'.
                int indexStart = p;
                char c;
                do {
                    checkArgument(p < format.length(), "dangling format characters in '%s'", format);
                    c = format.charAt(p++);
                }
                while (c >= '0' && c <= '9');
                int indexEnd = p - 1;

                // If 'c' doesn't take an argument, we're done.
                if (c == '$' || c == '>' || c == '<' || c == '[' || c == ']') {
                    checkArgument(indexStart == indexEnd, "$$, $>, $<, $[ and $] may not have an index");
                    formatParts.add("$" + c);
                    continue;
                }

                // Find either the indexed argument, or the relative argument. (0-based).
                int index;
                if (indexStart < indexEnd) {
                    index = Integer.parseInt(format.substring(indexStart, indexEnd)) - 1;
                    hasIndexed = true;
                    indexedParameterCount[index % args.length]++; // modulo is needed, checked below anyway
                }
                else {
                    index = relativeParameterCount;
                    hasRelative = true;
                    relativeParameterCount++;
                }

                checkArgument(index >= 0 && index < args.length,
                        "index %d for '%s' not in range (received %s arguments)",
                        index + 1, format.substring(indexStart - 1, indexEnd + 1), args.length);
                checkArgument(!hasIndexed || !hasRelative, "cannot mix indexed and positional parameters");

                switch (c) {
                    case 'N':
                        this.args.add(argToName(args[index]));
                        break;
                    case 'L':
                        this.args.add(argToLiteral(args[index]));
                        break;
                    case 'S':
                        this.args.add(argToString(args[index]));
                        break;
                    default:
                        throw new IllegalArgumentException(
                                String.format("invalid format string: '%s'", format));
                }

                formatParts.add("$" + c);
            }

            if (hasRelative) {
                checkArgument(relativeParameterCount >= args.length,
                        "unused arguments: expected %s, received %s", relativeParameterCount, args.length);
            }
            if (hasIndexed) {
                List<String> unused = new ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    if (indexedParameterCount[i] == 0) {
                        unused.add("$" + (i + 1));
                    }
                }
                String s = unused.size() == 1 ? "" : "s";
                checkArgument(unused.isEmpty(), "unused argument%s: %s", s, Util.join(", ", unused));
            }
            return this;
        }

        private String argToName(Object o)
        {
            if (o instanceof CharSequence) {
                return o.toString();
            }
            throw new IllegalArgumentException("expected name but was " + o);
        }

        private Object argToLiteral(Object o)
        {
            return o;
        }

        private String argToString(Object o)
        {
            return o != null ? String.valueOf(o) : null;
        }

        /**
         * @param controlFlow the control flow construct and its code, such as "if (foo == 5)".
         * Shouldn't contain braces or newline characters.
         */
        public Builder beginControlFlow(String controlFlow, Object... args)
        {
            add(controlFlow + " {\n", args);
            indent();
            return this;
        }

        /**
         * @param controlFlow the control flow construct and its code, such as "else if (foo == 10)".
         * Shouldn't contain braces or newline characters.
         */
        public Builder nextControlFlow(String controlFlow, Object... args)
        {
            unindent();
            add("} " + controlFlow + " {\n", args);
            indent();
            return this;
        }

        public Builder endControlFlow()
        {
            unindent();
            add("}\n");
            return this;
        }

        /**
         * @param controlFlow the optional control flow construct and its code, such as
         * "while(foo == 20)". Only used for "do/while" control flows.
         */
        public Builder endControlFlow(String controlFlow, Object... args)
        {
            unindent();
            add("} " + controlFlow + ";\n", args);
            return this;
        }

        public Builder addStatement(String format, Object... args)
        {
            add("$[");
            add(format, args);
            add(";\n$]");
            return this;
        }

        public Builder add(CodeBlock codeBlock)
        {
            formatParts.addAll(codeBlock.formatParts);
            args.addAll(codeBlock.args);
            return this;
        }

        public Builder indent()
        {
            this.formatParts.add("$>");
            return this;
        }

        public Builder unindent()
        {
            this.formatParts.add("$<");
            return this;
        }

        public CodeBlock build()
        {
            return new CodeBlock(this);
        }
    }
}
