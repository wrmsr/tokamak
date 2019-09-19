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
package com.wrmsr.tokamak.main.boot.dns;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ProxyNameService
        implements InvocationHandler
{
    protected volatile List<Object> original;

    public abstract InetAddress[] lookupAllHostAddr(String host)
            throws UnknownHostException;

    public abstract String getHostByAddr(byte[] addr)
            throws UnknownHostException;

    void setOriginal(List<Object> original)
    {
        this.original = original;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
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

    @SuppressWarnings({"unchecked"})
    public static void install(ProxyNameService dns)
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
        dns.setOriginal(original);
        nameServiceField.set(inetAddressClass, neu);
    }

    public void install()
            throws Exception
    {
        install(this);
    }

    public static final ProxyNameService replacing(Map<String, InetAddress[]> replacements)
    {
        return new ProxyNameService()
        {
            @Override
            public InetAddress[] lookupAllHostAddr(String host)
                    throws UnknownHostException
            {
                InetAddress[] rpl = replacements.get(host);
                if (rpl != null) {
                    return rpl;
                }

                try {
                    for (Object o : this.original) {
                        Method m = o.getClass().getDeclaredMethod("lookupAllHostAddr", String.class);
                        m.setAccessible(true);
                        return (InetAddress[]) m.invoke(o, host);
                    }
                    throw new UnknownHostException();
                }
                catch (UnknownHostException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getHostByAddr(byte[] addr)
                    throws UnknownHostException
            {
                try {
                    for (Object o : this.original) {
                        Method m = o.getClass().getDeclaredMethod("getHostByAddr", byte[].class);
                        m.setAccessible(true);
                        return (String) m.invoke(o, new Object[] {addr});
                    }
                    throw new UnknownHostException();
                }
                catch (UnknownHostException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
