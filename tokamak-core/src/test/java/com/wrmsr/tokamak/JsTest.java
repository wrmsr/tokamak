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

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.google.common.io.CharStreams;
import junit.framework.TestCase;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.io.InputStreamReader;

public class JsTest
        extends TestCase
{

    public void testJs()
            throws Throwable
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval("print('Hello World!');");
    }

    public void testV8()
            throws Throwable
    {
        V8 v8 = V8.createV8Runtime();
        V8Object object = v8.executeObjectScript("foo = {key: 'bar'}");

        String stringScript = v8.executeStringScript("'f'");
        System.out.println("Hello from Java! " + stringScript);

        Object result = object.get("key");
        assertTrue(result instanceof String);
        assertEquals("bar", result);
        object.release();

        v8.release();
    }

    public void testV8Blob()
            throws Throwable
    {
        String src = CharStreams.toString(new InputStreamReader(AppTest.class.getResourceAsStream("blob.js")));
        V8 v8 = V8.createV8Runtime();
        V8Object ret = v8.executeObjectScript(src);
        System.out.println(ret);
        String strRet = v8.executeStringScript("main()");
        System.out.println(strRet);
    }
}
