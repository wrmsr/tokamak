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
package com.wrmsr.tokamak.codegen.compile.javac;

import com.google.common.collect.Iterators;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static javax.tools.StandardLocation.CLASS_OUTPUT;

public class MemoryFileManager
        extends ForwardingJavaFileManager<JavaFileManager>
{
    private final Map<String, ClassMemoryJavaFileObject> mapNameToClasses = new HashMap<>();

    public MemoryFileManager(JavaFileManager fileManager)
    {
        super(fileManager);
    }

    private Collection<ClassMemoryJavaFileObject> memoryClasses()
    {
        return mapNameToClasses.values();
    }

    public JavaFileObject createSourceFileObject(Object origin, String name, String code)
    {
        return new MemoryJavaFileObject(origin, name, JavaFileObject.Kind.SOURCE, code);
    }

    public ClassLoader getClassLoader(JavaFileManager.Location location)
    {
        ClassLoader classLoader = super.getClassLoader(location);

        if (location == CLASS_OUTPUT) {
            Map<String, byte[]> mapNameToBytes = new HashMap<>();

            for (ClassMemoryJavaFileObject outputMemoryJavaFileObject : memoryClasses()) {
                mapNameToBytes.put(
                        outputMemoryJavaFileObject.getName(),
                        outputMemoryJavaFileObject.getBytes());
            }

            return new MemoryClassLoader(mapNameToBytes, classLoader);
        }

        return classLoader;
    }

    @Override
    public Iterable<JavaFileObject> list(
            JavaFileManager.Location location,
            String packageName,
            Set<JavaFileObject.Kind> kinds,
            boolean recurse)
            throws IOException
    {
        Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);

        if (location == CLASS_OUTPUT) {
            Collection<? extends JavaFileObject> generatedClasses = memoryClasses();
            return () -> Iterators.concat(
                    list.iterator(),
                    generatedClasses.iterator());
        }

        return list;
    }

    @Override
    public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file)
    {
        if (file instanceof ClassMemoryJavaFileObject) {
            return file.getName();
        }
        else {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling)
            throws IOException
    {
        if (kind == JavaFileObject.Kind.CLASS) {
            ClassMemoryJavaFileObject file = new ClassMemoryJavaFileObject(className);
            mapNameToClasses.put(className, file);
            return file;
        }

        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    static abstract class AbstractMemoryJavaFileObject
            extends SimpleJavaFileObject
    {
        public AbstractMemoryJavaFileObject(String name, JavaFileObject.Kind kind)
        {
            super(URI.create("memory:///" +
                    name.replace('.', '/') +
                    kind.extension), kind);
        }
    }

    static class MemoryJavaFileObject
            extends AbstractMemoryJavaFileObject
    {
        private final Object origin;
        private final String code;

        MemoryJavaFileObject(Object origin, String className, JavaFileObject.Kind kind, String code)
        {
            super(className, kind);

            this.origin = origin;
            this.code = code;
        }

        public Object getOrigin()
        {
            return origin;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
        {
            return code;
        }
    }

    static class ClassMemoryJavaFileObject
            extends AbstractMemoryJavaFileObject
    {

        private ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        private transient byte[] bytes = null;

        private final String className;

        public ClassMemoryJavaFileObject(String className)
        {
            super(className, JavaFileObject.Kind.CLASS);

            this.className = className;
        }

        public byte[] getBytes()
        {
            if (bytes == null) {
                bytes = byteOutputStream.toByteArray();
                byteOutputStream = null;
            }
            return bytes;
        }

        @Override
        public String getName()
        {
            return className;
        }

        @Override
        public OutputStream openOutputStream()
        {
            return byteOutputStream;
        }

        @Override
        public InputStream openInputStream()
        {
            return new ByteArrayInputStream(getBytes());
        }
    }
}
