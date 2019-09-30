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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

public final class OptoTest
{
    @FunctionalInterface
    public interface LongToLongFunction
    {
        long run(long l);
    }

    public interface Node
            extends LongToLongFunction
    {
    }

    public static final class NodeImpl
            implements Node
    {
        private final Node parent;
        private final LongToLongFunction task;

        public NodeImpl(Node parent, LongToLongFunction task)
        {
            this.parent = parent;
            this.task = task;
        }

        public long run(long l)
        {
            if (parent != null) {
                l = parent.run(l);
            }
            return task.run(l);
        }
    }

    public static Node create(Node parent, LongToLongFunction task, boolean specialize)
            throws Throwable
    {
        Class nodeCls = NodeImpl.class;

        if (specialize) {
            ForcingClassLoader cl = new ForcingClassLoader(OptoTest.class.getClassLoader());
            nodeCls = cl.forceReloadClass(nodeCls);
        }

        return (Node) nodeCls.getDeclaredConstructor(Node.class, LongToLongFunction.class).newInstance(parent, task);
    }

    public static void run(int depth, long iterations, boolean specialize)
            throws Throwable
    {
        Node node = null;

        for (int i = 0; i < depth; ++i) {
            node = create(node, ((Function<Integer, LongToLongFunction>) (m -> {
                long x = 13L + (2 * m);
                return l -> (l << m) ^ x;
            })).apply(i), specialize);
        }

        long l = 0x12478FED1923L;
        for (long i = 0; i < iterations; ++i) {
            l = node.run(l);
        }

        System.out.println(l);
    }

    public static final class ForcingClassLoader
            extends ClassLoader
    {
        public ForcingClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        public Class forceReloadClass(Class cls)
                throws Throwable
        {
            byte[] clsBytes;
            try (InputStream input = getResourceAsStream(cls.getName().replaceAll("\\.", "/") + ".class");
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int data = input.read();
                while (data != -1) {
                    buffer.write(data);
                    data = input.read();
                }
                clsBytes = buffer.toByteArray();
            }
            return defineClass(cls.getName(), clsBytes, 0, clsBytes.length);
        }
    }

    public static void main(String[] args)
            throws Throwable
    {
        // boolean specialize = true;
        boolean specialize = false;

        try (InputStreamReader isr = new InputStreamReader(System.in)) {
            while (isr.read() != '\n') {}
        }

        long start = System.currentTimeMillis();

        int depth = 8;
        long iterations = 2_000_000_000L;

        run(depth, iterations, specialize);

        long end = System.currentTimeMillis();
        System.out.println(String.format("%d ms", end - start));
    }
}
