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

public final class OptoTest
{
    public interface Task
    {
        long run(long l);
    }

    public static final class TaskA
            implements Task
    {
        @Override
        public long run(long l)
        {
            l <<= 1L;
            l ^= 13L;
            return l;
        }
    }

    public static final class TaskB
            implements Task
    {
        @Override
        public long run(long l)
        {
            l <<= 2L;
            l ^= 15L;
            return l;
        }
    }

    public static final class TaskC
            implements Task
    {
        @Override
        public long run(long l)
        {
            l <<= 3L;
            l ^= 17L;
            return l;
        }
    }

    public static final class TaskD
            implements Task
    {
        @Override
        public long run(long l)
        {
            l <<= 4L;
            l ^= 19L;
            return l;
        }
    }

    public interface Node
    {
        long run(long l);
    }

    public static final class NodeImpl
            implements Node
    {
        private final Node parent;
        private final Task task;

        public NodeImpl(Node parent, Task task)
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

    public static Node create(Node parent, Task task, boolean specialize)
            throws Throwable
    {
        Class nodeCls = NodeImpl.class;

        if (specialize) {
            ForcingClassLoader cl = new ForcingClassLoader(OptoTest.class.getClassLoader());
            nodeCls = cl.forceReloadClass(nodeCls);
        }

        return (Node) nodeCls.getDeclaredConstructor(Node.class, Task.class).newInstance(parent, task);
    }

    public static void run(boolean specialize)
            throws Throwable
    {
        Node node = null;

        node = create(node, new TaskA(), specialize);
        node = create(node, new TaskB(), specialize);
        node = create(node, new TaskC(), specialize);
        node = create(node, new TaskD(), specialize);

        long l = 0x12478FED1923L;
        for (long i = 0; i < 4_000_000_000L; ++i) {
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
        long start = System.currentTimeMillis();

        run(false);

        long end = System.currentTimeMillis();
        System.out.println(String.format("%d ms", end - start));
    }
}
