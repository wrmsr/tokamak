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
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdbi.v3.core.Jdbi;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.function.Supplier;

public class AppTest
        extends TestCase
{
    public AppTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite(AppTest.class);
    }

    public static final class IntForwarder
            implements Supplier<Integer>
    {
        private final Integer value;

        @Inject
        public IntForwarder(Integer value)
        {
            this.value = value;
        }

        @Override
        public Integer get()
        {
            return value;
        }
    }

    public static final class IntForwarderModule
            extends PrivateModule
    {
        private final String name;
        private final Integer value;

        public IntForwarderModule(String name, Integer value)
        {
            this.name = name;
            this.value = value;
        }

        @Override
        protected void configure()
        {
            bind(Integer.class).toInstance(value);
            bind(new TypeLiteral<Supplier<Integer>>()
            {
            }).annotatedWith(Names.named(name)).to(IntForwarder.class);
            expose(new TypeLiteral<Supplier<Integer>>()
            {
            }).annotatedWith(Names.named(name));
        }
    }

    public void testThing()
            throws Throwable
    {
        Injector injector = Guice.createInjector(new IntForwarderModule("a", 1), new IntForwarderModule("b", 2));
        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Supplier<Integer>>() {}, Names.named("a"))).get());
        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Supplier<Integer>>() {}, Names.named("b"))).get());
    }

    public void testJs()
            throws Throwable
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval("print('Hello World!');");
    }

    public void testJdbc()
            throws Throwable
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://0.0.0.0:21210", "tokamak", "tokamak")) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("select 420")) {
                    while (rs.next()) {
                        System.out.println(rs.getLong(1));
                    }
                }
            }
        }

        // TpchTable.SUPPLIER
    }

    public void testJdbi() throws Throwable
    {
        // DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");

        Jdbi jdbi = Jdbi.create("jdbc:mysql://0.0.0.0:21211", "root", "tokamak");

        jdbi.withHandle(handle -> {
            handle.execute("create database `tokamak`");
            return null;
        });
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

    public void testV8Blob() throws Throwable
    {
        String src = CharStreams.toString(new InputStreamReader(AppTest.class.getResourceAsStream("blob.js")));
        V8 v8 = V8.createV8Runtime();
        V8Object ret = v8.executeObjectScript(src);
        System.out.println(ret);
        String strRet = v8.executeStringScript("main()");
        System.out.println(strRet);
    }

    public void testTpch() throws Throwable
    {
        String ddl = CharStreams.toString(new InputStreamReader(AppTest.class.getResourceAsStream("tpch_ddl.sql")));

        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test", "username", "password");
        jdbi.withHandle(handle -> {
            // handle.execute("create database `tokamak`");
            for (String line : ddl.split(";")) {
                line = line.trim();
                if (line.startsWith("--")) {
                    continue;
                }
                handle.execute(line);
            }
            return null;
        });
    }
}
