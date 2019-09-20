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
package com.wrmsr.tokamak.main.boot;

import com.wrmsr.tokamak.main.boot.ops.ConfigureLoggingOp;
import com.wrmsr.tokamak.main.boot.ops.FixDnsOp;
import com.wrmsr.tokamak.main.boot.ops.PauseOp;
import com.wrmsr.tokamak.main.boot.ops.ReexecOp;
import com.wrmsr.tokamak.main.boot.ops.SetHeadlessOp;
import com.wrmsr.tokamak.util.config.Compilation;
import com.wrmsr.tokamak.util.config.ConfigMetadata;

import javax.annotation.CheckReturnValue;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Bootstrap
{
    /*
    TODO:
     - rlimits
     - jdk selection
    */

    private final BootstrapConfig config;
    private final Class<?> mainCls;
    private final String[] args;

    private Bootstrap(BootstrapConfig config, Class<?> mainCls, String[] args)
    {
        this.config = checkNotNull(config);
        this.mainCls = checkNotNull(mainCls);
        this.args = checkNotNull(args);
    }

    public BootstrapConfig getConfig()
    {
        return config;
    }

    public Class<?> getMainCls()
    {
        return mainCls;
    }

    public String[] getArgs()
    {
        return args;
    }

    public interface Op
    {
        void run()
                throws Exception;
    }

    private void run()
            throws Exception
    {
        for (Op op : new Op[] {
                new PauseOp(),
                new SetHeadlessOp(),
                new FixDnsOp(),
                new ConfigureLoggingOp(),
                new ReexecOp(mainCls.getCanonicalName(), args),
        }) {
            op.run();
        }
    }

    private static final Object LOCK = new Object();
    private static volatile Bootstrap INSTANCE = null;

    @CheckReturnValue
    public static String[] bootstrap(BootstrapConfig config, Class<?> mainCls, String[] args)
    {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    Bootstrap instance;
                    try {
                        instance = new Bootstrap(config, mainCls, args);
                        instance.run();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    INSTANCE = instance;
                }
            }
        }
        return args;
    }

    @CheckReturnValue
    public static String[] bootstrap(Class<?> mainCls, String[] args)
    {
        return bootstrap(getBootstrapConfig(), mainCls, args);
    }

    @SuppressWarnings({"unchecked"})
    public static BootstrapConfig getBootstrapConfig()
    {
        Class<? extends BootstrapConfig> bcImpl;
        try {
            bcImpl = (Class<? extends BootstrapConfig>) Class.forName(Compilation.getCompiledImplName(BootstrapConfig.class));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Compilation.ImplFactory<BootstrapConfig> bcFac = Compilation.getImplFactory(bcImpl);
        BootstrapConfig bc = bcFac.build(new ConfigMetadata(BootstrapConfig.class));
        return bc;
    }
}
