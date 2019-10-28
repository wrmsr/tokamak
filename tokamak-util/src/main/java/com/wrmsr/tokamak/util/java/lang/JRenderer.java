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
package com.wrmsr.tokamak.util.java.lang;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.MoreCollections;
import com.wrmsr.tokamak.util.java.lang.tree.JInheritance;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JAnnotatedDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JDeclarationBlock;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JDeclarationVisitor;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JField;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JInitializationBlock;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JMethod;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JRawDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JVerbatimDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JArrayAccess;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JAssignment;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JBinary;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JCast;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JConditional;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpression;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpressionVisitor;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JIdent;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLambda;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLiteral;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLongArrayLiteral;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLongStringLiteral;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMemberAccess;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMethodReference;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JNew;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JNewArray;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JRawExpression;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JUnary;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JAnnotatedStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JBlank;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JBlock;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JBreak;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JCase;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JContinue;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JDoWhileLoop;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JEmpty;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JForEach;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JIf;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JLabeledStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JRawStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JReturn;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JStatementVisitor;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JSwitch;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JThrow;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JVariable;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JWhileLoop;
import com.wrmsr.tokamak.util.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.util.java.lang.unit.JImportSpec;
import com.wrmsr.tokamak.util.java.lang.unit.JPackageSpec;
import com.wrmsr.tokamak.util.java.write.CodeBlock;
import com.wrmsr.tokamak.util.java.write.CodeWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class JRenderer
{
    public static final List<Set<JName>> DEFAULT_IMPORT_BLOCKS = ImmutableList.<Set<JName>>builder()
            .add(ImmutableSet.of(JName.of("javax")))
            .add(ImmutableSet.of(JName.of("java")))
            .build();

    public static final int DEFAULT_LONG_STRING_LITERAL_LENGTH = 120;
    public static final int DEFAULT_MULTILINE_PARAM_CUTOFF = 6;

    private final CodeBlock.Builder code;
    private final List<Set<JName>> importBlocks;
    private final int longStringLiteralLength;
    private final int multilineParamCutoff;

    public JRenderer(CodeBlock.Builder code, List<Set<JName>> importBlocks, int longStringLiteralLength, int multilineParamCutoff)
    {
        checkArgument(longStringLiteralLength > 0);
        checkArgument(multilineParamCutoff > 0);
        this.code = checkNotNull(code);
        this.importBlocks = ImmutableList.copyOf(importBlocks);
        this.longStringLiteralLength = longStringLiteralLength;
        this.multilineParamCutoff = multilineParamCutoff;
    }

    public JRenderer(CodeBlock.Builder code)
    {
        this(code, DEFAULT_IMPORT_BLOCKS, DEFAULT_LONG_STRING_LITERAL_LENGTH, DEFAULT_MULTILINE_PARAM_CUTOFF);
    }

    private <T> void delimitedForEach(Iterable<T> items, String delimiter, Consumer<T> consumer)
    {
        MoreCollections.delimitedForEach(items, () -> code.add(delimiter), consumer);
    }

    private void renderAccess(Set<JAccess> access)
    {
        code.add(
                Joiner.on("").join(
                        Arrays.stream(JAccess.values())
                                .filter(access::contains)
                                .map(a -> a.name().toLowerCase() + " ")
                                .collect(toImmutableList())));
    }

    public void renderCompilationUnit(JCompilationUnit compilationUnit)
    {
        compilationUnit.getPackageSpec().ifPresent(this::renderPackageSpec);
        code.add("\n");
        renderImportSpecs(compilationUnit.getImportSpecs().iterator());
        renderDeclaration(compilationUnit.getBody());
    }

    public void renderImportSpecs(Iterator<JImportSpec> importSpecs)
    {
        Set<JImportSpec> normals = new TreeSet<>();
        List<Set<JImportSpec>> blocks = importBlocks.stream().map(s -> new TreeSet<JImportSpec>()).collect(toImmutableList());
        Set<JImportSpec> statics = new TreeSet<>();
        outer:
        while (importSpecs.hasNext()) {
            JImportSpec importSpec = importSpecs.next();
            if (importSpec.isStatic()) {
                statics.add(importSpec);
                continue;
            }
            for (int i = 0; i < importBlocks.size(); ++i) {
                for (JName n : importBlocks.get(i)) {
                    if (importSpec.getName().startsWith(n)) {
                        blocks.get(i).add(importSpec);
                        continue outer;
                    }
                }
            }
            normals.add(importSpec);
        }
        if (!normals.isEmpty()) {
            normals.forEach(this::renderImportSpec);
            code.add("\n");
        }
        for (Set<JImportSpec> block : blocks) {
            if (!block.isEmpty()) {
                block.forEach(this::renderImportSpec);
                code.add("\n");
            }
        }
        if (!statics.isEmpty()) {
            statics.forEach(this::renderImportSpec);
            code.add("\n");
        }
    }

    public void renderImportSpec(JImportSpec importSpec)
    {
        code.add("import ");
        if (importSpec.isStatic()) {
            code.add("static ");
        }
        renderName(importSpec.getName());
        if (importSpec.isWildcard()) {
            code.add(".*");
        }
        code.add(";\n");
    }

    public void renderPackageSpec(JPackageSpec packageSpec)
    {
        code.add("package ");
        renderName(packageSpec.getName());
        code.add(";\n");
    }

    private void renderParams(List<JParam> params)
    {
        if (params.size() >= multilineParamCutoff) {
            code.add("(\n");
            code.indent().indent();
            delimitedForEach(params, ",\n", param -> {
                renderTypeSpecifier(param.getType());
                code.add(" $L", param.getName());
            });
            code.add(")");
            code.unindent().unindent();
        }
        else {
            code.add("(");
            delimitedForEach(params, ", ", param -> {
                renderTypeSpecifier(param.getType());
                code.add(" $L", param.getName());
            });
            code.add(")");
        }
    }

    public void renderDeclaration(JDeclaration curr)
    {
        curr.accept(new JDeclarationVisitor<Void, Void>()
        {
            @Override
            protected Void visitDeclaration(JDeclaration jstatement, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Void visitAnnotatedDeclaration(JAnnotatedDeclaration jdeclaration, Void context)
            {
                code.add("@");
                renderName(jdeclaration.getAnnotation());
                jdeclaration.getArgs().ifPresent(operands -> {
                    code.add("(");
                    renderOperands(operands);
                    code.add(")");
                });
                code.add("\n");
                renderDeclaration(jdeclaration.getDeclaration());
                return null;
            }

            @Override
            public Void visitConstructor(JConstructor jdeclaration, Void context)
            {
                renderAccess(jdeclaration.getAccess());
                code.add("$L", jdeclaration.getName());
                renderParams(jdeclaration.getParams());
                code.add("\n");
                renderStatement(jdeclaration.getBody());
                return null;
            }

            @Override
            public Void visitDeclarationBlock(JDeclarationBlock jdeclaration, Void context)
            {
                jdeclaration.getBody().forEach(JRenderer.this::renderDeclaration);
                return null;
            }

            @Override
            public Void visitField(JField jdeclaration, Void context)
            {
                renderAccess(jdeclaration.getAccess());
                renderTypeSpecifier(jdeclaration.getType());
                code.add(" $L", jdeclaration.getName());
                jdeclaration.getValue().ifPresent(v -> {
                    code.add(" = ");
                    renderExpression(v);
                });
                code.add(";\n");
                return null;
            }

            @Override
            public Void visitInitializationBlock(JInitializationBlock jdeclaration, Void context)
            {
                renderStatement(jdeclaration.getBlock());
                return null;
            }

            @Override
            public Void visitMethod(JMethod jdeclaration, Void context)
            {
                renderAccess(jdeclaration.getAccess());
                renderTypeSpecifier(jdeclaration.getType());
                code.add(" $L", jdeclaration.getName());
                renderParams(jdeclaration.getParams());
                if (jdeclaration.getBody().isPresent()) {
                    code.add("\n");
                    renderStatement(jdeclaration.getBody().get());
                }
                else {
                    code.add(";\n");
                }
                return null;
            }

            @Override
            public Void visitRawDeclaration(JRawDeclaration jdeclaration, Void context)
            {
                code.add(jdeclaration.getText());
                return null;
            }

            @Override
            public Void visitType(JType jdeclaration, Void context)
            {
                renderAccess(jdeclaration.getAccess());
                code.add("$L $L\n", jdeclaration.getKind().toString().toLowerCase(), jdeclaration.getName());
                if (!jdeclaration.getInheritances().isEmpty()) {
                    code.indent().indent();
                    boolean newline = false;
                    for (JInheritance jinheritance : jdeclaration.getInheritances()) {
                        if (newline) {
                            code.add(",\n");
                        }
                        else {
                            newline = true;
                        }
                        code.add("$L ", jinheritance.getKind().toString().toLowerCase());
                        renderName(jinheritance.getName());
                    }
                    code.add("\n");
                    code.unindent().unindent();
                }
                code.add("{\n");
                code.indent();
                boolean newline = false;
                for (JDeclaration d : jdeclaration.getBody()) {
                    if (newline) {
                        code.add("\n");
                    }
                    else {
                        newline = true;
                    }
                    renderDeclaration(d);
                }
                code.unindent();
                code.add("}\n");
                return null;
            }

            @Override
            public Void visitVerbatimDeclaration(JVerbatimDeclaration jdeclaration, Void context)
            {
                code.add("$L", jdeclaration.getText());
                return null;
            }
        }, null);
    }

    public void renderStatement(JStatement curr)
    {
        curr.accept(new JStatementVisitor<Void, Void>()
        {
            @Override
            protected Void visitStatement(JStatement jstatement, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Void visitAnnotatedStatement(JAnnotatedStatement jstatement, Void context)
            {
                code.add("@");
                renderName(jstatement.getAnnotation());
                jstatement.getArgs().ifPresent(operands -> {
                    code.add("(");
                    renderOperands(operands);
                    code.add(")");
                });
                code.add("\n");
                renderStatement(jstatement.getStatement());
                return null;
            }

            @Override
            public Void visitBlank(JBlank jstatement, Void context)
            {
                code.add("\n");
                return null;
            }

            @Override
            public Void visitBlock(JBlock jstatement, Void context)
            {
                code.add("{\n");
                code.indent();
                for (JStatement child : jstatement.getBody()) {
                    renderStatement(child);
                }
                code.unindent();
                code.add("}\n");
                return null;
            }

            @Override
            public Void visitBreak(JBreak jstatement, Void context)
            {
                if (jstatement.getLabel().isPresent()) {
                    code.addStatement("break $L", jstatement.getLabel().get());
                }
                else {
                    code.addStatement("break");
                }
                return null;
            }

            @Override
            public Void visitCase(JCase jstatement, Void context)
            {
                boolean newline = false;
                for (Object value : jstatement.getValues()) {
                    if (newline) {
                        code.add("\n");
                    }
                    else {
                        newline = true;
                    }
                    code.add("case ");
                    renderLiteralValue(value);
                    code.add(":");
                }
                if (jstatement.isDefault()) {
                    if (newline) {
                        code.add("\n");
                    }
                    code.add("default:");
                }
                code.add(" ");
                renderStatement(jstatement.getBlock());
                return null;
            }

            @Override
            public Void visitContinue(JContinue jstatement, Void context)
            {
                if (jstatement.getLabel().isPresent()) {
                    code.addStatement("continue $L", jstatement.getLabel().get());
                }
                else {
                    code.addStatement("continue");
                }
                return null;
            }

            @Override
            public Void visitDoWhileLoop(JDoWhileLoop jstatement, Void context)
            {
                code.add("do ");
                renderStatement(jstatement.getBody());
                code.add("while (");
                renderExpression(jstatement.getCondition());
                code.add(");\n");
                return null;
            }

            @Override
            public Void visitEmpty(JEmpty jstatement, Void context)
            {
                return null;
            }

            @Override
            public Void visitExpressionStatement(JExpressionStatement jstatement, Void context)
            {
                renderExpression(jstatement.getExpression());
                code.add(";\n");
                return null;
            }

            @Override
            public Void visitForEach(JForEach jstatement, Void context)
            {
                code.add("for (");
                renderTypeSpecifier(jstatement.getType());
                code.add(" $L : ", jstatement.getItem());
                renderExpression(jstatement.getIterable());
                code.add(") ");
                renderStatement(jstatement.getBody());
                return null;
            }

            @Override
            public Void visitIf(JIf jstatement, Void context)
            {
                code.add("if (");
                renderExpression(jstatement.getCondition());
                code.add(") ");
                renderStatement(jstatement.getIfTrue());
                if (jstatement.getIfFalse().isPresent()) {
                    code.add("else ");
                    renderStatement(jstatement.getIfFalse().get());
                }
                return null;
            }

            @Override
            public Void visitLabeledStatement(JLabeledStatement jstatement, Void context)
            {
                code.add("$L", jstatement.getLabel());
                code.add(":\n");
                renderStatement(jstatement.getStatement());
                return null;
            }

            @Override
            public Void visitRawStatement(JRawStatement jstatement, Void context)
            {
                code.add(jstatement.getText());
                return null;
            }

            @Override
            public Void visitReturn(JReturn jstatement, Void context)
            {
                if (jstatement.getValue().isPresent()) {
                    code.add("return ");
                    renderParamExpression(jstatement.getValue().get());
                    code.add(";\n");
                }
                else {
                    code.addStatement("return");
                }
                return null;
            }

            @Override
            public Void visitSwitch(JSwitch jstatement, Void context)
            {
                code.add("switch (");
                renderExpression(jstatement.getSelector());
                code.add(") ");
                renderStatement(new JBlock(ImmutableList.copyOf(jstatement.getCases())));
                return null;
            }

            @Override
            public Void visitThrow(JThrow jstatement, Void context)
            {
                code.add("throw ");
                renderParamExpression(jstatement.getException());
                code.add(";\n");
                return null;
            }

            @Override
            public Void visitVariable(JVariable jstatement, Void context)
            {
                renderTypeSpecifier(jstatement.getType());
                code.add(" $L", jstatement.getName());
                jstatement.getValue().ifPresent(v -> {
                    code.add(" = ");
                    renderExpression(v);
                });
                code.add(";\n");
                return null;
            }

            @Override
            public Void visitWhileLoop(JWhileLoop jstatement, Void context)
            {
                code.add("while (");
                renderExpression(jstatement.getCondition());
                code.add(") ");
                renderStatement(jstatement.getBody());
                return null;
            }
        }, null);
    }

    public void renderExpression(JExpression curr)
    {
        curr.accept(new JExpressionVisitor<Void, Void>()
        {
            @Override
            protected Void visitExpression(JExpression jexpression, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Void visitArrayAccess(JArrayAccess jexpression, Void context)
            {
                renderExpression(jexpression.getArray());
                code.add("[");
                renderExpression(jexpression.getIndex());
                code.add("]");
                return null;
            }

            @Override
            public Void visitAssignment(JAssignment jexpression, Void context)
            {
                renderExpression(jexpression.getLeft());
                code.add(" = ");
                renderExpression(jexpression.getRight());
                return null;
            }

            @Override
            public Void visitBinary(JBinary jexpression, Void context)
            {
                renderParamExpression(jexpression.getLeft());
                code.add(" ");
                code.add(jexpression.getOp().getString());
                code.add(" ");
                renderParamExpression(jexpression.getRight());
                return null;
            }

            @Override
            public Void visitCast(JCast jexpression, Void context)
            {
                code.add("(");
                renderTypeSpecifier(jexpression.getType());
                code.add(") ");
                renderParamExpression(jexpression.getValue());
                return null;
            }

            @Override
            public Void visitConditional(JConditional jexpression, Void context)
            {
                renderParamExpression(jexpression.getCondition());
                code.add(" ? ");
                renderParamExpression(jexpression.getIfTrue());
                code.add(" : ");
                renderParamExpression(jexpression.getIfFalse());
                return null;
            }

            @Override
            public Void visitIdent(JIdent jexpression, Void context)
            {
                renderName(jexpression.getName());
                return null;
            }

            @Override
            public Void visitLambda(JLambda jexpression, Void context)
            {
                code.add("(");
                delimitedForEach(jexpression.getParams(), ", ", code::add);
                code.add(") -> ");
                renderStatement(jexpression.getBody());
                return null;
            }

            @Override
            public Void visitLiteral(JLiteral jexpression, Void context)
            {
                renderLiteralValue(jexpression.getValue());
                return null;
            }

            @Override
            public Void visitLongArrayLiteral(JLongArrayLiteral jexpression, Void context)
            {
                if (jexpression.getItems().isEmpty()) {
                    code.add("{}");
                }
                else {
                    code.add("{\n");
                    code.indent().indent();
                    delimitedForEach(jexpression.getItems(), ",\n", JRenderer.this::renderExpression);
                    code.unindent().unindent();
                    code.add("\n}");
                }
                return null;
            }

            @Override
            public Void visitLongStringLiteral(JLongStringLiteral jexpression, Void context)
            {
                String str = jexpression.getValue();
                code.add("\"\" + \n");
                code.indent().indent();
                for (int i = 0; i < str.length(); i += longStringLiteralLength) {
                    if (i > 0) {
                        code.add(" +\n");
                    }
                    code.add("$S", str.substring(i, Math.min(i + longStringLiteralLength, str.length())));
                }
                code.unindent().unindent();
                return null;
            }

            @Override
            public Void visitMemberAccess(JMemberAccess jexpression, Void context)
            {
                renderExpression(jexpression.getInstance());
                code.add(".$L", jexpression.getMember());
                return null;
            }

            @Override
            public Void visitMethodInvocation(JMethodInvocation jexpression, Void context)
            {
                renderExpression(jexpression.getMethod());
                code.add("(");
                renderOperands(jexpression.getArgs());
                code.add(")");
                return null;
            }

            @Override
            public Void visitMethodReference(JMethodReference jexpression, Void context)
            {
                renderExpression(jexpression.getInstance());
                code.add("::$L", jexpression.getMethodName());
                return null;
            }

            @Override
            public Void visitNew(JNew jexpression, Void context)
            {
                code.add("new ");
                renderTypeSpecifier(jexpression.getType());
                code.add("(");
                renderOperands(jexpression.getArgs());
                code.add(")");
                return null;
            }

            @Override
            public Void visitNewArray(JNewArray jexpression, Void context)
            {
                code.add("new ");
                renderTypeSpecifier(jexpression.getType());
                jexpression.getItems().ifPresent(i -> {
                    code.add(" {");
                    renderOperands(i);
                    code.add("}");
                });
                return null;
            }

            @Override
            public Void visitRawExpression(JRawExpression jexpression, Void context)
            {
                code.add(jexpression.getText());
                return null;
            }

            @Override
            public Void visitUnary(JUnary jexpression, Void context)
            {
                code.add(jexpression.getOp().getPrefix());
                renderOperands(ImmutableList.of(jexpression.getValue()));
                code.add(jexpression.getOp().getSuffix());
                return null;
            }
        }, null);
    }

    public void renderParamExpression(JExpression curr)
    {
        curr.accept(new JExpressionVisitor<Void, Void>()
        {
            @Override
            protected Void visitExpression(JExpression jexpression, Void context)
            {
                code.add("(");
                renderExpression(jexpression);
                code.add(")");
                return null;
            }

            @Override
            public Void visitIdent(JIdent jexpression, Void context)
            {
                renderExpression(jexpression);
                return null;
            }

            @Override
            public Void visitLiteral(JLiteral jexpression, Void context)
            {
                renderExpression(jexpression);
                return null;
            }
        }, null);
    }

    public void renderTypeSpecifier(JTypeSpecifier type)
    {
        renderName(type.getName());
        type.getGenerics().ifPresent(g -> {
            code.add("<");
            delimitedForEach(g, ", ", this::renderTypeSpecifier);
            code.add(">");
        });
        type.getArrays().forEach(a -> {
            code.add("[");
            a.getSize().ifPresent(this::renderExpression);
            code.add("]");
        });
    }

    public void renderName(JName name)
    {
        code.add("$L", Joiner.on('.').join(name.getParts()));
    }

    public void renderLiteralValue(Object value)
    {
        if (value instanceof String) {
            code.add("$S", value);
        }
        else {
            code.add(Objects.toString(value)); // FIXME lol
            if (value instanceof Float) {
                code.add("f");
            }
            else if (value instanceof Long) {
                code.add("L");
            }
        }
    }

    public void renderOperands(List<JExpression> operands)
    {
        delimitedForEach(operands, ", ", this::renderParamExpression);
    }

    public static void renderWithIndent(CodeBlock block, String indent, Appendable appendable)
    {
        try {
            CodeWriter.builder(appendable).indent(indent).build().emit(block);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String renderWithIndent(CodeBlock block, String indent)
    {
        StringWriter out = new StringWriter();
        renderWithIndent(block, indent, out);
        return out.toString();
    }

    public static void renderWithIndent(Consumer<JRenderer> renderer, String indent, Appendable appendable)
    {
        CodeBlock.Builder code = CodeBlock.builder();
        renderer.accept(new JRenderer(code));
        CodeBlock block = code.build();
        renderWithIndent(block, indent, appendable);
    }

    public static String renderWithIndent(JCompilationUnit jcompilationUnit, String indent)
    {
        StringWriter out = new StringWriter();
        renderWithIndent(r -> r.renderCompilationUnit(jcompilationUnit), indent, out);
        return out.toString();
    }

    public static String renderWithIndent(JDeclaration jdeclaration, String indent)
    {
        StringWriter out = new StringWriter();
        renderWithIndent(r -> r.renderDeclaration(jdeclaration), indent, out);
        return out.toString();
    }
}
