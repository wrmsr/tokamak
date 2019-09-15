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

import com.wrmsr.tokamak.util.io.HttpClient;
import com.wrmsr.tokamak.util.io.JdkHttpClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;

public class HttpTest
{
    @Path("helloworld")
    @Produces("text/plain")
    public static class HelloWorldResource
    {
        public static final String CLICHED_MESSAGE = "Hello World!";

        @GET
        public String get(
                @Context UriInfo urlInfo,
                @Context ContainerRequest request,
                @Context MyObj myObj,
                @Context ChannelHandlerContext channelHandlerContext)
        {
            return CLICHED_MESSAGE;
        }

        @POST
        public String post(final String entity)
        {
            return entity;
        }

        @PUT
        public void put(final String entity)
        {
            // NOOP
        }

        @Path("locator")
        public Class<?> sub()
        {
            return HelloWorldResource.class;
        }
    }

    static class MyObj
    {
    }

    static class MyObjFactory implements Factory<MyObj>
    {
        private ContainerRequestContext requestContext;

        @Inject
        public MyObjFactory(ContainerRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public MyObj provide() {
            MyObj obj = new MyObj();
            return obj;
        }

        @Override
        public void dispose(MyObj instance)
        {
        }

        public static class Binder extends AbstractBinder
        {
            @Override
            protected void configure() {
                bindFactory(MyObjFactory.class).to(MyObj.class).in(RequestScoped.class);
            }
        }
    }

    public static void main(String[] args)
            throws Throwable
    {
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class);
        resourceConfig.register(new MyObjFactory.Binder());
        Channel server = NettyHttpContainerProvider.createServer(baseUri, resourceConfig, false);

        Thread.sleep(500);

        HttpClient.Response resp = new JdkHttpClient()
                .request("localhost", 9998, HttpClient.Request.of("GET", "helloworld"));
        System.out.println(resp);

        Thread.sleep(300000);

        server.close().sync();

        // server.closeFuture().sync();
    }
}
