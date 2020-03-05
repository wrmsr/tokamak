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

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.util.Jdk;
import com.wrmsr.tokamak.util.java.compile.javac.InProcJavaCompiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class LoomTest
{
    private static final Class<?> continuationScopeCls;
    private static final Constructor<?> continuationScopeClsCtor;
    private static final Class<?> continuationCls;
    private static final Constructor<?> continuationCtor;
    private static final Method continuationRun;
    private static final Method continuationYield;
    private static final Method continuationGetCurrentContinuation;

    static {
        try {
            System.setProperty("java.lang.Continuation.trace", "true");
            System.setProperty("java.lang.Continuation.debug", "true");

            continuationScopeCls = Class.forName("java.lang.ContinuationScope");
            continuationScopeClsCtor = continuationScopeCls.getDeclaredConstructor(String.class);
            continuationCls = Class.forName("java.lang.Continuation");
            continuationCtor = continuationCls.getDeclaredConstructor(continuationScopeCls, Runnable.class);
            continuationRun = continuationCls.getDeclaredMethod("run");
            continuationYield = continuationCls.getDeclaredMethod("yield", continuationScopeCls);
            continuationGetCurrentContinuation = continuationCls.getDeclaredMethod("getCurrentContinuation", continuationScopeCls);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class EchoServerLoom
    {
        // private static final java.lang.ContinuationScope scope = new ContinuationScope() {};

        private static final Object scope;

        static {
            try {
                scope = continuationScopeClsCtor.newInstance("LoomTest");
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void main(String[] args)
                throws Exception
        {
            ServerSocket server = new ServerSocket(5566);
            while (true) {
                Socket client = server.accept();
                EchoHandlerLoom handler = new EchoHandlerLoom(client);

                // Continuation continuation = new Continuation(scope, handler);
                // continuation.run();

                Object continuation = continuationCtor.newInstance(scope, handler);
                continuationRun.invoke(continuation);
            }
        }
    }

    public static class EchoHandlerLoom
            implements Runnable
    {
        private final Socket client;

        EchoHandlerLoom(Socket client)
        {
            this.client = client;
        }

        @Override
        public void run()
        {
            try {
                byte[] output = new byte[64];
                try (InputStream in = new java.net.URL("http://google.com").openStream()) {
                    ByteStreams.readFully(in, output);
                }

                Object currentContinuation = continuationGetCurrentContinuation.invoke(null, EchoServerLoom.scope);
                continuationYield.invoke(currentContinuation, EchoServerLoom.scope);

                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                writer.println("[echo] " + Arrays.toString(output));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    client.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        String src = CharStreams.toString(new InputStreamReader(LoomTest.class.getResourceAsStream("/com/wrmsr/tokamak/test/LoomTest15.java.txt")));
        Class<?> cls = InProcJavaCompiler.compileAndLoadFromTempFile(
                src,
                "com.wrmsr.tokamak.test.LoomTest15",
                "LoomTest15",
                ImmutableList.of("-classpath", Jdk.getClasspath(), "-g:lines,source,vars"),
                LoomTest.class.getClassLoader());
        cls.getDeclaredMethod("main", String[].class).invoke(null, new Object[] {args});

        // EchoServerLoom.main(args);
    }
}
