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
package com.wrmsr.tokamak.main.server.util.jaxrs;

import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycle;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.TokamakNettyHttpContainerProvider;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class NettyServer
        extends AbstractLifecycle
{
    private final Logger log = Logger.get(NettyServer.class);

    private final Application application;
    private Channel server;

    @Inject
    public NettyServer(Application application)
    {
        this.application = checkNotNull(application);
    }

    public Channel getServer()
    {
        return server;
    }

    @Override
    protected void doStart()
            throws Exception
    {
        int port = 9998;
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        server = TokamakNettyHttpContainerProvider.createServer(baseUri, application, null, ah -> {}, sb -> {});
        log.info("Listening on port %d", port);
    }

    public void requestStop()
    {
        checkState(isStarted());
        log.info("Requested stop");
        server.close();
    }

    @Override
    protected void doStop()
            throws Exception
    {
        log.info("Awaiting stop");
        server.close().sync();
        log.info("Stopped");
    }
}
