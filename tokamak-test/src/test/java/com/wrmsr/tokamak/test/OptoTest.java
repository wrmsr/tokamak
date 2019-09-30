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
    public interface I
    {
        long run(long l);
    }

    public static final class IA
            implements I
    {
        @Override
        public long run(long l)
        {
            l <<= 1L;
            l ^= 13L;
            return l;
        }
    }

    public static final class IB
            implements I
    {
        @Override
        public long run(long l)
        {
            l <<= 2L;
            l ^= 15L;
            return l;
        }
    }

    public static final class IC
            implements I
    {
        @Override
        public long run(long l)
        {
            l <<= 3L;
            l ^= 17L;
            return l;
        }
    }

    public static final class ID
            implements I
    {
        @Override
        public long run(long l)
        {
            l <<= 4L;
            l ^= 19L;
            return l;
        }
    }

    public interface P
    {
        long run(long l);
    }

    public static final class PImpl
            implements P
    {
        private final P parent;
        private final I o;

        public PImpl(P parent, I o)
        {
            this.parent = parent;
            this.o = o;
        }

        public long run(long l)
        {
            if (parent != null) {
                l = parent.run(l);
            }
            return o.run(l);
        }
    }

    public static P create(P parent, I o)
            throws Throwable
    {
        Class pcls;

        pcls = PImpl.class;
        // CL cl = new CL(OptoTest.class.getClassLoader());
        // pcls = cl.reloadClass(PImpl.class);

        return (P) pcls.getDeclaredConstructor(P.class, I.class).newInstance(parent, o);
    }

    public static void run()
            throws Throwable
    {
        P p = null;

        p = create(p, new IA());
        p = create(p, new IB());
        p = create(p, new IC());
        p = create(p, new ID());

        long l = 0x12478FED1923L;
        for (long i = 0; i < 4_000_000_000L; ++i) {
            l = p.run(l);
        }

        System.out.println(l);
    }

    public static final class CL
            extends ClassLoader
    {
        public CL(ClassLoader parent)
        {
            super(parent);
        }

        public Class reloadClass(Class cls)
                throws Throwable
        {
            byte[] classData;
            try (InputStream input = getResourceAsStream(cls.getName().replaceAll("\\.", "/") + ".class");
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int data = input.read();
                while (data != -1) {
                    buffer.write(data);
                    data = input.read();
                }
                classData = buffer.toByteArray();
            }
            return defineClass(cls.getName(), classData, 0, classData.length);
        }
    }

    public static void main(String[] args)
            throws Throwable
    {
        long start = System.currentTimeMillis();

        run();

        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
