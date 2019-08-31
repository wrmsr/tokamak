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
package com.wrmsr.tokamak.java;

import com.google.common.base.Joiner;

import javax.annotation.concurrent.Immutable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public final class Jars
{
    private Jars()
    {
    }

    public static final int BUF_SIZE = 65536;

    // FIXME: META-INF/MANIFEST.MF Class-Path
    // https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html

    public static void buildJar(java.io.File destination, List<BuildEntry> entries)
            throws IOException
    {
        Map<String, BuildEntry> entryMap = entries.stream().collect(toImmutableMap(BuildEntry::getName, identity()));
        List<String> entryNames = new ArrayList<>(entryMap.keySet());
        Collections.sort(entryNames);

        Set<String> contents = new HashSet<>();
        try (FileOutputStream fos = new FileOutputStream(destination);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                JarOutputStream jos = new JarOutputStream(bos)) {
            for (String name : entryNames) {
                checkState(!contents.contains(name));
                BuildEntry entry = entryMap.get(name);
                checkState(name.equals(entry.getName()));
                List<String> pathParts = newArrayList(name.split("/"));
                for (int i = 0; i < pathParts.size() - 1; ++i) {
                    String pathPart = Joiner.on("/").join(IntStream.rangeClosed(0, i).boxed().map(j -> pathParts.get(j)).collect(Collectors.toList())) + "/";
                    if (!contents.contains(pathPart)) {
                        JarEntry je = new JarEntry(pathPart);
                        jos.putNextEntry(je);
                        jos.write(new byte[] {}, 0, 0);
                        contents.add(pathPart);
                    }
                }
                JarEntry jarEntry = new JarEntry(name);
                jos.putNextEntry(jarEntry);
                if (entry instanceof BuildEntry.File) {
                    BuildEntry.File fileEntry = (BuildEntry.File) entry;
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileEntry.getFile()))) {
                        byte[] buf = new byte[BUF_SIZE];
                        int anz;
                        while ((anz = bis.read(buf)) != -1) {
                            jos.write(buf, 0, anz);
                        }
                    }
                }
                else if (entry instanceof BuildEntry.Bytes) {
                    BuildEntry.Bytes bytesEntry = (BuildEntry.Bytes) entry;
                    jos.write(bytesEntry.getBytes(), 0, bytesEntry.getBytes().length);
                }
                else {
                    throw new IllegalStateException();
                }
                contents.add(name);
            }
        }
    }

    @Immutable
    public abstract static class BuildEntry
    {
        private final String name;
        private final long time;

        public BuildEntry(String name, long time)
        {
            checkArgument(time >= 0);
            this.name = requireNonNull(name);
            this.time = time;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BuildEntry entry = (BuildEntry) o;
            return time == entry.time &&
                    Objects.equals(name, entry.name);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, time);
        }

        public String getName()
        {
            return name;
        }

        public long getTime()
        {
            return time;
        }

        @Immutable
        public static final class Bytes
                extends BuildEntry
        {
            private final byte[] bytes;

            public Bytes(String name, long time, byte[] bytes)
            {
                super(name, time);
                this.bytes = requireNonNull(bytes);
            }

            public byte[] getBytes()
            {
                return bytes;
            }
        }

        @Immutable
        public static final class File
                extends BuildEntry
        {
            private final java.io.File file;

            public File(String name, java.io.File file)
            {
                super(name, file.lastModified());
                this.file = file;
            }

            public java.io.File getFile()
            {
                return file;
            }
        }
    }
}
