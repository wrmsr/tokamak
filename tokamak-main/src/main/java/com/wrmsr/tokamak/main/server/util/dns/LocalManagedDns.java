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
package com.wrmsr.tokamak.main.server.util.dns;

import com.google.common.base.Throwables;
import com.wrmsr.tokamak.util.Logger;
import sun.net.spi.nameservice.NameService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class LocalManagedDns
        implements NameService
{
    private static final Logger log = Logger.get(LocalManagedDns.class);

    private final NameService defaultDnsImpl;
    private static final Pattern ipPattern = Pattern.compile("ip(-[0-9]{1,3}){4}");

    public LocalManagedDns()
    {
        try {
            Class<? extends NameService> clazz = (Class<? extends NameService>) Class.forName("sun.net.spi.nameservice.dns.DNSNameService");
            this.defaultDnsImpl = clazz.getConstructor().newInstance();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getHostByAddr(byte[] ip)
            throws UnknownHostException
    {
        log.debug("");

        return defaultDnsImpl.getHostByAddr(ip);
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String s)
            throws UnknownHostException
    {
        if (ipPattern.matcher(s).matches()) {
            String ip = s.substring(3).replace('-', '.');
            return InetAddress.getAllByName(ip);
        }
        String ipAddress = NameStore.getInstance().get(s);
        if (!ipAddress.isEmpty()) {
            log.debug("\tmatch");
            return InetAddress.getAllByName(ipAddress);
        }
        else {
            log.debug("\tmiss");
            return defaultDnsImpl.lookupAllHostAddr(s);
        }
    }
}
