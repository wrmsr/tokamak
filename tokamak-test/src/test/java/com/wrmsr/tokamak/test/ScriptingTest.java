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
package com.wrmsr.tokamak.test;

import junit.framework.TestCase;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptingTest
        extends TestCase
{
    public void testGroovy()
            throws Throwable
    {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");

        Integer sum = (Integer) engine.eval("(1..10).sum()");
        assertEquals(new Integer(55), sum);

        {
            engine.put("first", "HELLO");
            engine.put("second", "world");
            String result = (String) engine.eval("first.toLowerCase() + ' ' + second.toUpperCase()");
            assertEquals("hello WORLD", result);
        }

        String fact = "def factorial(n) { n == 1 ? 1 : n * factorial(n - 1) }";
        engine.eval(fact);

        {
            Invocable inv = (Invocable) engine;
            Object[] params = {5};
            Object result = inv.invokeFunction("factorial", params);
            assertEquals(new Integer(120), result);
        }
    }
}
