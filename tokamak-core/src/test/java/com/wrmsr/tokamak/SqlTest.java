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
package com.wrmsr.tokamak;

import com.wrmsr.tokamak.util.RequiresBase;
import junit.framework.TestCase;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class SqlTest
        extends TestCase
{
    /*
    Reflective matching sql nodes is for external extensibility (dialects). Internal tokamak nodes do not require this.
    Emulate kwarg with derive4j.
    */

    public interface Visitable
    {
    }

    public interface Visitor<R>
    {
        default R visit(Visitable visitable)
        {
            throw new IllegalArgumentException(visitable.toString());
        }
    }

    public static abstract class TypeEngine
            implements Visitable
    {
    }

    public static class DbInteger
            extends TypeEngine
    {
    }

    public static class SmallInteger
            extends TypeEngine
    {
    }

    public static class BigInteger
            extends TypeEngine
    {
    }

    public static abstract class SchemaItem
            implements Visitable
    {
    }

    @RequiresBase({ClauseElement.class})
    public interface Executable
            extends Visitable
    {
    }

    public static abstract class ClauseElement
            implements Visitable
    {
    }

    public static class Select
            extends ClauseElement
    {
    }

    public static class Update
            extends ClauseElement
    {
    }

    public static class TestVisitor
            implements Visitor
    {
        // public String visit(ClauseElement clauseElement)
        // {
        //     return super.visit(clauseElement);
        // }

        public String visit(Select select)
        {
            return "select";
        }

        public String visit(Update update)
        {
            return "update";
        }
    }

    @Test
    public void testVisit()
            throws Throwable
    {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        // MethodType mt = MethodType.methodType(String.class, char.class, char.class);
        // MethodHandle replaceMH = publicLookup.findVirtual(String.class, "replace", mt);

        // String output = (String) replaceMH.invoke("jovo", 'o', 'a');
        // assertEquals("java", output);

        Select select = new Select();
        Visitor visitor = new TestVisitor();

        Method visitSelectMethod = Stream.of(TestVisitor.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("visit"))
                .filter(m -> m.getReturnType() == String.class)
                .filter(m -> m.getParameterTypes()[0] == Select.class)
                .findFirst()
                .get();

        MethodHandle visitSelectMethodHandle = publicLookup.unreflect(visitSelectMethod);

        String output = (String) visitSelectMethodHandle.invoke(visitor, select);
        assertEquals("select", output);
    }
}
