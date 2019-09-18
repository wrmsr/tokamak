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
package com.wrmsr.tokamak.main.bootstrap.dns;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreFiles.writeTempFile;
import static java.util.function.UnaryOperator.identity;

public final class Dns
{
    /*
    TODO:
     - check / warn
    */

    private Dns()
    {
    }

    public static Path rewriteHostsFile(Path path, Map<String, String> replacements)
            throws IOException
    {
        List<String> lines = Files.readAllLines(path);

        String out = Joiner.on("\n").join(
                ImmutableList.<String>builder().addAll(
                        lines.stream().map(l -> {
                            List<String> parts = Splitter.on(CharMatcher.whitespace()).splitToList(l);
                            if (replacements.isEmpty()) {
                                return "";
                            }
                            String rpl = replacements.get(parts.get(0));
                            if (rpl != null) {
                                return "";
                            }
                            else {
                                return l;
                            }
                        }).collect(toImmutableList()))
                        .addAll(replacements.values())
                        .build());

        return writeTempFile("hosts", out.getBytes(Charsets.UTF_8));
    }

    public static final Path DEFAULT_HOSTS_FILE = Paths.get("/etc/hosts");

    public static final Set<String> DEFAULT_HOSTS_REPLACEMENT_LOCALHOST_KEYS = ImmutableSet.of(
            "127.0.0.1",
            "::1"
    );

    public static Map<String, String> getDefaultHostsReplacement(String localHostname)
    {
        return DEFAULT_HOSTS_REPLACEMENT_LOCALHOST_KEYS.stream().collect(toImmutableMap(identity(), k -> k + " localhost " + localHostname));
    }

    public static final String HOSTS_FILE_PROPERTY_KEY = "jdk.net.hosts.file";

    public static void fixPosixLocalhostHostsFile()
            throws Exception
    {
        String localHostname = LocalHostnameGetter.SUBPROCESS.get();
        Path out = rewriteHostsFile(DEFAULT_HOSTS_FILE, getDefaultHostsReplacement(localHostname));
        System.setProperty(HOSTS_FILE_PROPERTY_KEY, out.toAbsolutePath().toString());
    }
}
