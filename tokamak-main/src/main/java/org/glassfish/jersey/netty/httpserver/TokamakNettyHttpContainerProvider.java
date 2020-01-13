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

package org.glassfish.jersey.netty.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

import java.net.URI;
import java.util.function.Consumer;

public final class TokamakNettyHttpContainerProvider
{
    private TokamakNettyHttpContainerProvider()
    {
    }

    public static Channel createServer(
            URI baseUri,
            Application application,
            SslContext sslContext,
            Consumer<ApplicationHandler> applicationHandlerConsumer,
            Consumer<ServerBootstrap> serverBootstrapConsumer)
            throws ProcessingException
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        NettyHttpContainer container = new NettyHttpContainer(application);
        applicationHandlerConsumer.accept(container.getApplicationHandler());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new JerseyServerInitializer(baseUri, sslContext, container, ResourceConfig.forApplication(application)));
            serverBootstrapConsumer.accept(b);

            int port = getPort(baseUri);

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>()
            {
                @Override
                public void operationComplete(Future<? super Void> future)
                        throws Exception
                {
                    container.getApplicationHandler().onShutdown(container);

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            return ch;
        }
        catch (InterruptedException e) {
            throw new ProcessingException(e);
        }
    }

    private static int getPort(URI uri)
    {
        if (uri.getPort() == -1) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                return 80;
            }
            else if ("https".equalsIgnoreCase(uri.getScheme())) {
                return 443;
            }

            throw new IllegalArgumentException("URI scheme must be 'http' or 'https'.");
        }

        return uri.getPort();
    }
}
