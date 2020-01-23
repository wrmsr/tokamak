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
package com.wrmsr.tokamak.main.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.dist.GitRevision;
import org.glassfish.hk2.api.Immediate;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Immediate
@Path("/v1")
public class RootResource
{
    private final Uptime.Service uptimeService;

    @Inject
    public RootResource(Uptime.Service uptimeService)
    {
        this.uptimeService = uptimeService;
    }

    @GET
    @Path("/status")
    @Produces(APPLICATION_JSON)
    public Status status()
    {
        return new Status(
                uptimeService.getUptimeMillis() / 1000.0f,
                GitRevision.get(),
                Optional.ofNullable(System.getProperty("java.version")));
    }

    @POST
    @Path("/query")
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_JSON)
    public QueryResult query(String query)
    {
        return new QueryResult(ImmutableMap.of(), 0, ImmutableList.of());
    }

    @POST
    @Path("/shutdown")
    public void shutdown(String query)
    {
    }
}
