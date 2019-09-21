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

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import junit.framework.TestCase;

import java.util.Date;
import java.util.List;

public class PortScanTest
        extends TestCase
{
    public class Decoder
            extends ByteToMessageDecoder
    {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        {
            if (in.readableBytes() < 4) {
                return;
            }

            out.add(in.readBytes(4));
        }
    }

    public static class ClientHandler
            extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
        {
            ByteBuf m = (ByteBuf) msg; // (1)
            try {
                long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
                System.out.println(new Date(currentTimeMillis));
                ctx.close();
            }
            finally {
                m.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static class Client
    {
        public void run()
                throws Exception
        {
            String host = "localhost";
            int port = 8000;
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception
                    {
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

                ChannelFuture f = b.connect(host, port).sync();

                f.channel().closeFuture().sync();
            }
            finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public static class ServerHandler
            extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelActive(final ChannelHandlerContext ctx)
        {
            final ByteBuf time = ctx.alloc().buffer(4);
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

            final ChannelFuture f = ctx.writeAndFlush(time);
            f.addListener(new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture future)
                {
                    assert f == future;
                    ctx.close();
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static class Server
    {
        public void run()
                throws Exception
        {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>()
                        {
                            @Override
                            public void initChannel(SocketChannel ch)
                                    throws Exception
                            {
                                ch.pipeline().addLast(new ServerHandler());
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture f = b.bind(8000).sync();
                f.channel().closeFuture().sync();
            }
            finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }
    }

    public void testPortScan()
            throws Throwable
    {
        Thread serverThread = new Thread(() -> {
            try {
                new Server().run();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
        Thread.sleep(5000);

        Thread clientThread = new Thread(() -> {
            try {
                new Client().run();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
        Thread.sleep(60000);
    }
}
