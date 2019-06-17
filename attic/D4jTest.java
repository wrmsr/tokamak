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
package com.wrmsr.tokamak;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.util.Json;
import org.derive4j.Data;
import org.junit.Test;

import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

public class D4jTest
{
    @JsonTypeInfo(use = NAME, include = PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Requests.GET.class, name = "get"),
            @JsonSubTypes.Type(value = Requests.DELETE.class, name = "delete"),
            @JsonSubTypes.Type(value = Requests.PUT.class, name = "put"),
            @JsonSubTypes.Type(value = Requests.POST.class, name = "post")
    })
    @Data
    public static abstract class Request
    {
        interface Cases<R>
        {
            R GET(String path);

            R DELETE(String path);

            R PUT(String path, String body);

            R POST(String path, String body);
        }

        public abstract <R> R match(Cases<R> cases);
    }

    @Test
    public void testD4J()
            throws Throwable
    {
        Request r = Requests.GET("abc");
        System.out.println(r);
        System.out.println(Json.toJson(r));

        Function<Request, Integer> getBodySize = Requests.cases()
                .PUT((path, body) -> body.length())
                .POST((path, body) -> body.length())
                .otherwise_(0);
        System.out.println(getBodySize.apply(r));
    }
}
