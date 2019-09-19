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
package com.wrmsr.tokamak.main;

import com.wrmsr.tokamak.main.boot.Bootstrap;
import com.wrmsr.tokamak.main.boot.BootstrapConfig;
import com.wrmsr.tokamak.util.config.Compilation;
import com.wrmsr.tokamak.util.config.ConfigMetadata;

public class Main
{
    public static void main(String[] args)
            throws Throwable
    {
        Bootstrap.bootstrap();

        @SuppressWarnings({"unchecked"})
        Class<? extends BootstrapConfig> bcImpl = (Class<? extends BootstrapConfig>) Class.forName(Compilation.getCompiledImplName(BootstrapConfig.class));
        Compilation.ImplFactory<BootstrapConfig> bcFac = Compilation.getImplFactory(bcImpl);
        BootstrapConfig bc = bcFac.build(new ConfigMetadata(BootstrapConfig.class));
        bc.reexec();

        CliMain.main(args);
    }
}
