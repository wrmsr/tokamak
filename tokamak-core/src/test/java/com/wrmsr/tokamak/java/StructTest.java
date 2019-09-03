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
package com.wrmsr.tokamak.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JArg;
import com.wrmsr.tokamak.java.lang.JName;
import com.wrmsr.tokamak.java.lang.JRenderer;
import com.wrmsr.tokamak.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.java.lang.tree.declaration.JAnnotatedDeclaration;
import com.wrmsr.tokamak.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.java.lang.tree.declaration.JField;
import com.wrmsr.tokamak.java.lang.tree.declaration.JMethod;
import com.wrmsr.tokamak.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.java.lang.tree.expression.JAssignment;
import com.wrmsr.tokamak.java.lang.tree.expression.JIdent;
import com.wrmsr.tokamak.java.lang.tree.statement.JBlock;
import com.wrmsr.tokamak.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.java.lang.tree.statement.JReturn;
import com.wrmsr.tokamak.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.java.lang.unit.JPackageSpec;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Sets.immutableEnumSet;

public class StructTest
        extends TestCase
{
    /*
    TODO:
     - varhandle array faster than switch?
      - lol gen VarHandles when present, don't use directly
     - 'manifest' obj - factory, field info, etc
      - class StructSpec: fields, final, etc?
    */

    public interface Struct
    {
        boolean getBoolean(int position);

        void setBoolean(int position, boolean value);

        long getLong(int position);

        void setLong(int position, long value);

        double getDouble(int position);

        void setDouble(int position, double value);

        byte[] getBytes(int position);

        void setBytes(int position, byte[] value);

        String setString(int position);

        void setString(int position, String value);
    }

    public final class MutableThingStruct
            implements Struct
    {
        private boolean someBoolean0;
        private long someLong0;
        private double someDouble0;
        private byte[] someBytes0;
        private String someString0;

        private String someString1;
        private byte[] someBytes1;
        private double someDouble1;
        private long someLong1;
        private boolean someBoolean1;

        @Override
        public boolean getBoolean(int position)
        {
            switch (position) {
                case 0:
                    return someBoolean0;
                case 9:
                    return someBoolean1;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public void setBoolean(int position, boolean value)
        {
            switch (position) {
                case 0:
                    someBoolean0 = value;
                    return;
                case 9:
                    someBoolean1 = value;
                    return;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public long getLong(int position)
        {
            switch (position) {
                case 1:
                    return someLong0;
                case 8:
                    return someLong1;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public void setLong(int position, long value)
        {
            switch (position) {
                case 1:
                    someLong0 = value;
                    return;
                case 8:
                    someLong1 = value;
                    return;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public double getDouble(int position)
        {
            switch (position) {
                case 2:
                    return someDouble0;
                case 7:
                    return someDouble1;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public void setDouble(int position, double value)
        {
            switch (position) {
                case 2:
                    someDouble0 = value;
                    return;
                case 7:
                    someDouble1 = value;
                    return;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public byte[] getBytes(int position)
        {
            switch (position) {
                case 3:
                    return someBytes0;
                case 6:
                    return someBytes1;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public void setBytes(int position, byte[] value)
        {
            switch (position) {
                case 3:
                    someBytes0 = value;
                    return;
                case 6:
                    someBytes1 = value;
                    return;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public String setString(int position)
        {
            switch (position) {
                case 4:
                    return someString0;
                case 5:
                    return someString1;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }

        @Override
        public void setString(int position, String value)
        {
            switch (position) {
                case 4:
                    someString0 = value;
                    return;
                case 5:
                    someString1 = value;
                    return;
                default:
                    throw new IllegalArgumentException(Objects.toString(position));
            }
        }
    }

    // public void testVarHandles()
    //         throws Throwable
    // {
    //     VarHandle vh = MethodHandles
    //             .privateLookupIn(MutableThingStruct.class, MethodHandles.lookup())
    //             .findVarHandle(MutableThingStruct.class, "someLong", long.class);
    //
    //     MutableThingStruct t = new MutableThingStruct();
    //     System.out.println(t.someLong0);
    //     vh.set(420L);
    //     System.out.println(t.someLong0);
    // }

    public void testStruct()
            throws Throwable
    {
        Map<String, Class<?>> fields = ImmutableMap.<String, Class<?>>builder()
                .put("someBoolean0", boolean.class)
                .put("someLong0", long.class)
                .put("someDouble0", double.class)
                .put("someBytes0", byte[].class)
                .put("someString0", String.class)

                .put("someString1", String.class)
                .put("someBytes1", byte[].class)
                .put("someDouble1", double.class)
                .put("someLong1", long.class)
                .put("someBoolean1", boolean.class)

                .build();

        ImmutableList.Builder<JDeclaration> decls = ImmutableList.builder();

        for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
            decls.add(
                    new JField(
                            immutableEnumSet(JAccess.PRIVATE, JAccess.FINAL),
                            JTypeSpecifier.of(entry.getValue().toString()),
                            entry.getKey(),
                            Optional.empty()));
        }

        decls.add(
                new JConstructor(
                        immutableEnumSet(JAccess.PUBLIC),
                        "Thing",
                        ImmutableList.of(),
                        JBlock.EMPTY));

        for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
            decls.add(
                    new JAnnotatedDeclaration(
                            JName.of("Override"),
                            Optional.empty(),
                            new JMethod(
                                    immutableEnumSet(JAccess.PUBLIC),
                                    JTypeSpecifier.of(entry.getValue().toString()),
                                    "get" + entry.getKey(),
                                    ImmutableList.of(),
                                    Optional.of(
                                            new JBlock(
                                                    ImmutableList.of(
                                                            new JReturn(
                                                                    Optional.of(
                                                                            new JIdent(
                                                                                    JName.of(entry.getKey()))))
                                                    ))))));

            decls.add(
                    new JMethod(
                            immutableEnumSet(JAccess.PUBLIC),
                            JTypeSpecifier.of("void"),
                            "set" + entry.getKey(),
                            ImmutableList.of(
                                    new JArg(
                                            JTypeSpecifier.of(entry.getValue().toString()),
                                            entry.getKey())
                            ),
                            Optional.of(
                                    new JBlock(
                                            ImmutableList.of(
                                                    new JExpressionStatement(
                                                            new JAssignment(
                                                                    new JIdent(
                                                                            JName.of("this", entry.getKey())),
                                                                    new JIdent(
                                                                            JName.of(entry.getKey()))))
                                            )))));
        }

        JCompilationUnit cu = new JCompilationUnit(
                Optional.of(new JPackageSpec(JName.of("com", "wrmsr", "tokamak"))),
                ImmutableSet.of(),
                new JType(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.FINAL),
                        JType.Kind.CLASS,
                        "Thing",
                        ImmutableList.of(),
                        decls.build()));

        String rendered = JRenderer.renderWithIndent(cu, "    ");
        System.out.println(rendered);
    }
}
