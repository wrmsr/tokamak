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
package com.wrmsr.tokamak.server;

import com.google.common.collect.ImmutableMap;
import me.bazhenov.groovysh.GroovyShellService;
import org.junit.Test;

public class GshTest
{
    @Test
    public void testGsh()
            throws Throwable
    {
        GroovyShellService service = new GroovyShellService();
        service.setPort(6789);
        service.setBindings(ImmutableMap.of(
                "foo", 420,
                "bar", "bong"
        ));

        service.start();
        // Thread.currentThread().sleep(60000);
        service.destroy();
    }
}
