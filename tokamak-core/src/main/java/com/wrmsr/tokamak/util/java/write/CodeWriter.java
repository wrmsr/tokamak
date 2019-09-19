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
package com.wrmsr.tokamak.util.java.write;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.java.write.Util.stringLiteralWithDoubleQuotes;

public final class CodeWriter
{
    /**
     * Sentinel value that indicates that no user-provided package has been set.
     */
    private static final String NO_PACKAGE = new String();

    private final String indent;
    private final Appendable out;
    /**
     * When emitting a statement, this is the line of the statement currently being written. The first
     * line of a statement is indented normally and subsequent wrapped lines are double-indented. This
     * is -1 when the currently-written line isn't part of a statement.
     */
    int statementLine = -1;
    private int indentLevel;
    private boolean javadoc = false;
    private boolean comment = false;
    private String packageName = NO_PACKAGE;
    private boolean trailingNewline;

    private CodeWriter(Builder builder)
    {
        this.out = builder.out;
        this.indent = builder.indent;
    }

    private static String extractMemberName(String part)
    {
        checkArgument(Character.isJavaIdentifierStart(part.charAt(0)), "not an identifier: %s", part);
        for (int i = 1; i <= part.length(); i++) {
            if (!SourceVersion.isIdentifier(part.substring(0, i))) {
                return part.substring(0, i - 1);
            }
        }
        return part;
    }

    public static Builder builder(Appendable out)
    {
        checkNotNull(out, "packageName == null");
        return new Builder(out);
    }

    public CodeWriter indent()
    {
        return indent(1);
    }

    public CodeWriter indent(int levels)
    {
        indentLevel += levels;
        return this;
    }

    public CodeWriter unindent()
    {
        return unindent(1);
    }

    public CodeWriter unindent(int levels)
    {
        checkArgument(indentLevel - levels >= 0, "cannot unindent %s from %s", levels, indentLevel);
        indentLevel -= levels;
        return this;
    }

    public CodeWriter pushPackage(String packageName)
    {
        checkState(this.packageName == NO_PACKAGE, "package already set: %s", this.packageName);
        this.packageName = checkNotNull(packageName, "packageName == null");
        return this;
    }

    public CodeWriter popPackage()
    {
        checkState(this.packageName != NO_PACKAGE, "package already set: %s", this.packageName);
        this.packageName = NO_PACKAGE;
        return this;
    }

    public void emitComment(CodeBlock codeBlock)
            throws IOException
    {
        trailingNewline = true; // Force the '//' prefix for the comment.
        comment = true;
        try {
            emit(codeBlock);
            emit("\n");
        }
        finally {
            comment = false;
        }
    }

    public void emitJavadoc(CodeBlock javadocCodeBlock)
            throws IOException
    {
        if (javadocCodeBlock.isEmpty()) {
            return;
        }

        emit("/**\n");
        javadoc = true;
        try {
            emit(javadocCodeBlock);
        }
        finally {
            javadoc = false;
        }
        emit(" */\n");
    }

    /**
     * Emits {@code modifiers} in the standard order. Modifiers in {@code implicitModifiers} will not
     * be emitted.
     */
    public void emitModifiers(Set<Modifier> modifiers, Set<Modifier> implicitModifiers)
            throws IOException
    {
        if (modifiers.isEmpty()) {
            return;
        }
        for (Modifier modifier : EnumSet.copyOf(modifiers)) {
            if (implicitModifiers.contains(modifier)) {
                continue;
            }
            emitAndIndent(modifier.name().toLowerCase(Locale.US));
            emitAndIndent(" ");
        }
    }

    public void emitModifiers(Set<Modifier> modifiers)
            throws IOException
    {
        emitModifiers(modifiers, Collections.<Modifier>emptySet());
    }

    public CodeWriter emit(String s)
            throws IOException
    {
        return emitAndIndent(s);
    }

    public CodeWriter emit(String format, Object... args)
            throws IOException
    {
        return emit(CodeBlock.of(format, args));
    }

    public CodeWriter emit(CodeBlock codeBlock)
            throws IOException
    {
        int a = 0;
        ListIterator<String> partIterator = codeBlock.formatParts.listIterator();
        while (partIterator.hasNext()) {
            String part = partIterator.next();
            switch (part) {
                case "$L":
                    emitLiteral(codeBlock.args.get(a++));
                    break;

                case "$N":
                    emitAndIndent((String) codeBlock.args.get(a++));
                    break;

                case "$S":
                    String string = (String) codeBlock.args.get(a++);
                    // Emit null as a literal null: no quotes.
                    emitAndIndent(string != null
                            ? stringLiteralWithDoubleQuotes(string, indent)
                            : "null");
                    break;

                case "$$":
                    emitAndIndent("$");
                    break;

                case "$>":
                    indent();
                    break;

                case "$<":
                    unindent();
                    break;

                case "$[":
                    checkState(statementLine == -1, "statement enter $[ followed by statement enter $[");
                    statementLine = 0;
                    break;

                case "$]":
                    checkState(statementLine != -1, "statement exit $] has no matching statement enter $[");
                    if (statementLine > 0) {
                        unindent(2); // End a multi-line statement. Decrease the indentation level.
                    }
                    statementLine = -1;
                    break;

                default:
                    // handle deferred type
                    emitAndIndent(part);
                    break;
            }
        }
        return this;
    }

    private void emitLiteral(Object o)
            throws IOException
    {
        if (o instanceof CodeBlock) {
            CodeBlock codeBlock = (CodeBlock) o;
            emit(codeBlock);
        }
        else {
            emitAndIndent(String.valueOf(o));
        }
    }

    /**
     * Emits {@code s} with indentation as required. It's important that all code that writes to
     * {@link #out} does it through here, since we emit indentation lazily in order to avoid
     * unnecessary trailing whitespace.
     */
    CodeWriter emitAndIndent(String s)
            throws IOException
    {
        boolean first = true;
        for (String line : s.split("\n", -1)) {
            // Emit a newline character. Make sure blank lines in Javadoc & comments look good.
            if (!first) {
                if ((javadoc || comment) && trailingNewline) {
                    emitIndentation();
                    out.append(javadoc ? " *" : "//");
                }
                out.append('\n');
                trailingNewline = true;
                if (statementLine != -1) {
                    if (statementLine == 0) {
                        indent(2); // Begin multiple-line statement. Increase the indentation level.
                    }
                    statementLine++;
                }
            }

            first = false;
            if (line.isEmpty()) {
                continue; // Don't indent empty lines.
            }

            // Emit indentation and comment prefix if necessary.
            if (trailingNewline) {
                emitIndentation();
                if (javadoc) {
                    out.append(" * ");
                }
                else if (comment) {
                    out.append("// ");
                }
            }

            out.append(line);
            trailingNewline = false;
        }
        return this;
    }

    private void emitIndentation()
            throws IOException
    {
        for (int j = 0; j < indentLevel; j++) {
            out.append(indent);
        }
    }

    public static final class Builder
    {
        private final Appendable out;
        private String indent = "  ";

        private Builder(Appendable out)
        {
            this.out = checkNotNull(out, "out == null");
        }

        public Builder indent(String indent)
        {
            this.indent = checkNotNull(indent, "indent == null");
            return this;
        }

        public CodeWriter build()
        {
            return new CodeWriter(this);
        }
    }
}
