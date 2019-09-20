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
package com.wrmsr.tokamak.main.boot.ops;

import com.wrmsr.tokamak.main.boot.Bootstrap;
import com.wrmsr.tokamak.main.boot.dns.Dns;
import com.wrmsr.tokamak.util.Jdk;

public final class FixDnsOp
        implements Bootstrap.Op
{
    @Override
    public void run()
            throws Exception
    {
        if (Jdk.getMajor() > 8) {
            Dns.fixPosixLocalhostHostsFile();
        }
        else {
            Dns.hookDnsToFixLocalhost();
        }
    }
}
