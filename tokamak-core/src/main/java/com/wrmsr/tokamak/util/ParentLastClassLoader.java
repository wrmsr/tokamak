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
import java.net.URLClassLoader;
import java.util.List;

public class ParentLastClassLoader
        extends ClassLoader
{
    private ChildURLClassLoader childClassLoader;

    public ChildURLClassLoader getChildClassLoader()
    {
        return childClassLoader;
    }

    /**
     * This class allows me to call findClass on a classloader
     */
    private static class FindClassClassLoader
            extends ClassLoader
    {
        public FindClassClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name)
                throws ClassNotFoundException
        {
            return super.findClass(name);
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a URLClassLoader.
     * We need this because findClass is protected in URLClassLoader
     */
    public static class ChildURLClassLoader
            extends URLClassLoader
    {
        private FindClassClassLoader realParent;

        public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent)
        {
            super(urls, null);

            this.realParent = realParent;
        }

        public FindClassClassLoader getRealParent()
        {
            return realParent;
        }

        @Override
        public Class<?> findClass(String name)
                throws ClassNotFoundException
        {
            try {
                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            }
            catch (ClassNotFoundException e) {
                // if that fails, we ask our real parent classloader to load the class (we give up)
                return realParent.loadClass(name);
            }
        }
    }

    public ParentLastClassLoader(List<URL> classpath)
    {
        super(Thread.currentThread().getContextClassLoader());

        URL[] urls = classpath.toArray(new URL[classpath.size()]);

        childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        try {
            // first we try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        }
        catch (ClassNotFoundException e) {
            // didn't find it, try the parent
            return super.loadClass(name, resolve);
        }
    }
}