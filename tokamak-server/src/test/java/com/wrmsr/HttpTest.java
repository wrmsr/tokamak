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
package com.wrmsr;

import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

public class HttpTest
{
    @Path("helloworld")
    @Produces("text/plain")
    public static class HelloWorldResource
    {
        public static final String CLICHED_MESSAGE = "Hello World!";

        @GET
        public String get()
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

    public static void main(String[] args)
            throws Throwable
    {
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class);
        Channel server = NettyHttpContainerProvider.createServer(baseUri, resourceConfig, false);
    }
}
