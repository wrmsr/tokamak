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
package com.wrmsr.tokamak.core;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.google.common.io.CharStreams;
import junit.framework.TestCase;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import java.io.InputStreamReader;
import java.nio.file.Path;

import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public class JsTest
        extends TestCase
{
    public void testJs()
            throws Throwable
    {
        //https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html
        //https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/javascript.html

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");

        engine.eval("print('Hello World!');");
        engine.eval("function add2(v) { return v + 2; }");
        engine.eval("f(2)");
        // engine.eval("f(x)", );

        engine.put("x", "hello");
        // print global variable "x"
        engine.eval("println(x);");
        // the above line prints "hello"

        // Now, pass a different script context
        ScriptContext newContext = new SimpleScriptContext();
        Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        // add new variable "x" to the new engineScope
        engineScope.put("x", "world");

        // execute the same script - but this time pass a different script context
        engine.eval("println(x);", newContext);
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
        //https://github.com/browserify/browserify
        //https://cran.r-project.org/web/packages/V8/vignettes/v8_intro.html
        //https://cran.r-project.org/web/packages/V8/vignettes/npm.html
        //https://github.com/jeroen/V8/blob/a9308d21b087a0ac38e6babe7ff01bc7d98c43c1/src/legacy/V8.cpp#L114

        Path tmp = createTempDirectory("tokamak-v8");
        V8 v8 = V8.createV8Runtime(null, tmp.toString());

        String src = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("blob.js.txt")));
        V8Function ret = (V8Function) v8.executeObjectScript(src);
        System.out.println(ret);

        V8Object lodash = (V8Object) v8.executeScript("_");

        V8Object receiver = v8.executeObjectScript("{}");
        V8Array parameters = new V8Array(v8);
        Object ret2 = ret.call(receiver, parameters);
        parameters.release();

        // String strRet = v8.executeStringScript("main()");
        // System.out.println(strRet);
    }
}
