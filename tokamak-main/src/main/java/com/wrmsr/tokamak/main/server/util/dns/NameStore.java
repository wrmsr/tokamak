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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NameStore
{
    protected static NameStore singleton;

    protected Map globalNames;
    protected ThreadLocal localThread;

    protected NameStore()
    {
        globalNames = Collections.synchronizedMap(new HashMap());
        localThread = new ThreadLocal();
    }

    public static NameStore getInstance()
    {
        if (singleton == null) {
            synchronized (NameStore.class) {
                if (singleton == null) {
                    singleton = new NameStore();
                }
            }
        }
        return singleton;
    }

    public void put(String hostName, String ipAddress)
    {
        globalNames.put(hostName, ipAddress);
    }

    public void remove(String hostName)
    {
        globalNames.remove(hostName);
    }

    public synchronized void putLocal(String hostName, String ipAddress)
    {
        Map localThreadNames = (Map) localThread.get();
        if (localThreadNames == null) {
            localThreadNames = Collections.synchronizedMap(new HashMap());
            localThread.set(localThreadNames);
        }
        localThreadNames.put(hostName, ipAddress);
    }

    public void removeLocal(String hostName)
    {
        Map localThreadNames = (Map) localThread.get();
        if (localThreadNames != null) {
            localThreadNames.remove(hostName);
        }
    }

    public String get(String hostName)
    {
        String ipAddress = null;
        Map localThreadNames = (Map) localThread.get();
        if (localThreadNames != null) {
            ipAddress = (String) localThreadNames.get(hostName);
        }
        if (ipAddress.isEmpty()) {
            return (String) globalNames.get(hostName);
        }
        return ipAddress;
    }
}
