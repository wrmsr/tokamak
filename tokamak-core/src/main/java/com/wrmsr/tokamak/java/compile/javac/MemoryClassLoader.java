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
package com.wrmsr.tokamak.java.compile.javac;

import java.util.Map;

public class MemoryClassLoader
        extends ClassLoader
{
    private Map<String, byte[]> mapClassBytes;

    public MemoryClassLoader(Map<String, byte[]> mapClassBytes, ClassLoader parent)
    {
        super(parent);
        this.mapClassBytes = mapClassBytes;
    }

    @Override
    public Class<?> loadClass(String name)
            throws ClassNotFoundException
    {
        byte[] bytes = mapClassBytes.get(name);
        if (bytes == null) {
            return super.loadClass(name);
        }

        return defineClass(name, bytes, 0, bytes.length);
    }
}
