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
package com.wrmsr.tokamak.server.util.jaxrs;

import com.wrmsr.tokamak.util.Logger;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.TokamakNettyHttpContainerProvider;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class NettyServer
{
    private final Logger log = Logger.get(NettyServer.class);

    private final Application application;

    @Inject
    public NettyServer(Application application)
    {
        this.application = checkNotNull(application);
    }

    public void run()
            throws InterruptedException
    {
        int port = 9998;
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        Channel server = TokamakNettyHttpContainerProvider.createServer(baseUri, application, null, ah -> {}, sb -> {});
        log.info("Listening on port %d", port);
        server.closeFuture().sync();
    }
}
