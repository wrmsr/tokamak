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
package com.wrmsr.tokamak.util;

import java.net.URL;
import java.util.Arrays;

public final class DelegatingClassLoader
        extends ClassLoader
{
    private final ClassLoader delegates[];

    public DelegatingClassLoader(ClassLoader parent, ClassLoader... delegates)
    {
        super(parent);
        this.delegates = delegates;
    }

    @Override
    public Class<?> loadClass(String clazz)
            throws ClassNotFoundException
    {

        if (getParent() != null) {
            try {
                return getParent().loadClass(clazz);
            }
            catch (ClassNotFoundException cnfe) {
            }
        }

        ClassNotFoundException firstFail = null;
        for (ClassLoader delegate : delegates) {
            try {
                return delegate.loadClass(clazz);
            }
            catch (ClassNotFoundException ncfe) {
                if (firstFail == null) {
                    firstFail = ncfe;
                }
            }
        }

        if (firstFail != null) {
            throw firstFail;
        }
        throw new ClassNotFoundException("Could not find " + clazz);
    }

    @Override
    public URL getResource(String resource)
    {
        if (getParent() != null) {
            URL u = getParent().getResource(resource);

            if (u != null) {
                return u;
            }
        }

        for (ClassLoader delegate : delegates) {
            URL u = delegate.getResource(resource);

            if (u != null) { return u; }
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "DelegatingClassLoader{" +
                "delegates=" + Arrays.toString(delegates) +
                '}';
    }
}
