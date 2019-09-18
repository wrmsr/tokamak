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
package com.wrmsr.tokamak.main.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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

    public static Path patchHostsFile(Path path, Map<String, String> rpls)
            throws IOException
    {
        List<String> lines = Files.readAllLines(path);

        String out = Joiner.on("\n").join(
                ImmutableList.<String>builder().addAll(
                        lines.stream().map(l -> {
                            List<String> parts = Splitter.on(CharMatcher.whitespace()).splitToList(l);
                            if (rpls.isEmpty()) {
                                return "";
                            }
                            String rpl = rpls.get(parts.get(0));
                            if (rpl != null) {
                                return "";
                            }
                            else {
                                return l;
                            }
                        }).collect(toImmutableList()))
                        .addAll(rpls.values())
                        .build());

        return writeTempFile("hosts", out.getBytes(Charsets.UTF_8));
    }

    public static final Path DEFAULT_HOSTS_FILE = Paths.get("/etc/hosts");

    public static final Set<String> DEFAULT_HOSTS_REPLACEMENT_LOCALHOST_KEYS = ImmutableSet.of(
            "127.0.0.1",
            "::1"
    );

    public static Map<String, String> getDefaultHostsReplacement()
            throws Exception
    {
        InetAddress inetAddress = InetAddress.getLocalHost();
        String hostname = inetAddress.getHostName();
        return DEFAULT_HOSTS_REPLACEMENT_LOCALHOST_KEYS.stream().collect(toImmutableMap(identity(), k -> k + " localhost " + hostname));
    }

    public static final String HOSTS_FILE_PROPERTY_KEY = "jdk.net.hosts.file";

    public static void fixPosixLocalhostHostsFile()
            throws Exception
    {
        Path out = patchHostsFile(DEFAULT_HOSTS_FILE, getDefaultHostsReplacement());
        System.setProperty(HOSTS_FILE_PROPERTY_KEY, out.toAbsolutePath().toString());
    }

    public interface ProxyNameService
            extends InvocationHandler
    {
        InetAddress[] lookupAllHostAddr(String host)
                throws UnknownHostException;

        String getHostByAddr(byte[] addr)
                throws UnknownHostException;

        @Override
        default Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            switch (method.getName()) {
                case "lookupAllHostAddr":
                    return lookupAllHostAddr((String) args[0]);
                case "getHostByAddr":
                    return getHostByAddr((byte[]) args[0]);
                default:
                    StringBuilder o = new StringBuilder();
                    o.append(method.getReturnType().getCanonicalName()).append(" ").append(method.getName()).append("(");
                    Class<?>[] ps = method.getParameterTypes();
                    for (int i = 0; i < ps.length; ++i) {
                        if (i > 0) {
                            o.append(", ");
                        }
                        o.append(ps[i].getCanonicalName()).append(" p").append(i);
                    }
                    o.append(")");
                    throw new UnsupportedOperationException(o.toString());
            }
        }
    }

    public static abstract class AbstractProxyNameService
            implements ProxyNameService
    {
        private volatile List<Object> original;

        public void setOriginal(List<Object> original)
        {
            this.original = original;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static void installProxyNameService(ProxyNameService dns)
            throws Exception
    {
        Class<?> inetAddressClass = InetAddress.class;
        List<Object> original;
        Object neu;
        Field nameServiceField;
        try {
            Class<?> iface = Class.forName("java.net.InetAddress$NameService");
            nameServiceField = inetAddressClass.getDeclaredField("nameService");
            nameServiceField.setAccessible(true);
            original = ImmutableList.of(nameServiceField.get(inetAddressClass));
            neu = Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, dns);
        }
        catch (ClassNotFoundException | NoSuchFieldException e) {
            Class<?> iface = Class.forName("sun.net.spi.nameservice.NameService");
            nameServiceField = inetAddressClass.getDeclaredField("nameServices");
            nameServiceField.setAccessible(true);
            original = (List<Object>) nameServiceField.get(inetAddressClass);
            neu = Collections.singletonList(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, dns));
        }
        if (dns instanceof AbstractProxyNameService) {
            ((AbstractProxyNameService) dns).setOriginal(original);
        }
        nameServiceField.set(inetAddressClass, neu);
    }
}
