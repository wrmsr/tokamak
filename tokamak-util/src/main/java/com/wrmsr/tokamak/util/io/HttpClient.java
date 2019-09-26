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
package com.wrmsr.tokamak.util.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.wrmsr.tokamak.util.NoExceptAutoCloseable;
import com.wrmsr.tokamak.util.json.Json;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public interface HttpClient
        extends NoExceptAutoCloseable
{
    @Immutable
    final class Request
    {
        private final String method;
        private final String path;
        private final Map<String, String> headers;

        @Nullable
        private final Auth auth;

        @Nullable
        private final byte[] data;

        public Request(
                String method,
                String path,
                Map<String, String> headers,
                @Nullable Auth auth,
                @Nullable byte[] data)
        {
            this.method = checkNotNull(method);
            this.path = checkNotNull(path);
            this.headers = ImmutableMap.copyOf(headers);
            this.auth = auth;
            this.data = data;
        }

        @Override
        public String toString()
        {
            return "Request{" +
                    "method='" + method + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }

        public String getMethod()
        {
            return method;
        }

        public String getPath()
        {
            return path;
        }

        public Map<String, String> getHeaders()
        {
            return headers;
        }

        @Nullable
        public Auth getAuth()
        {
            return auth;
        }

        @Nullable
        public byte[] getData()
        {
            return data;
        }

        public static Request of(String method, String path)
        {
            return new Request(method, path, ImmutableMap.of(), null, null);
        }

        public static Request of(String method, String path, byte[] data)
        {
            return new Request(method, path, ImmutableMap.of(), null, data);
        }
    }

    @Immutable
    final class Auth
    {
        private final String username;
        private final String password;

        public Auth(String username, String password)
        {
            this.username = checkNotNull(username);
            this.password = checkNotNull(password);
        }

        @Override
        public String toString()
        {
            return "Auth{" +
                    "username='" + username + '\'' +
                    '}';
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

        public String getEncoded()
        {
            String joined = username + ":" + password;
            return BaseEncoding.base64().encode(joined.getBytes());
        }

        public static Auth of(String username, String password)
        {
            return new Auth(username, password);
        }
    }

    final class Response
    {
        private final int code;

        @Nullable
        private final byte[] data;

        public Response(int code, @Nullable byte[] data)
        {
            this.code = code;
            this.data = data;
        }

        @Override
        public String toString()
        {
            return "Response{" +
                    "code=" + code +
                    '}';
        }

        public int getCode()
        {
            return code;
        }

        @Nullable
        public byte[] getData()
        {
            return data;
        }

        public String getDataUtf8()
        {
            return new String(data, Charsets.UTF_8);
        }

        public <T> T getDataJson(TypeReference<T> type)
        {
            try {
                return Json.OBJECT_MAPPER_SUPPLIER.get().readValue(data, type);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public <T> T getDataJson(Class<T> cls)
        {
            return getDataJson(Json.typeReference(cls));
        }

        public Object getDataJson()
        {
            return getDataJson(Object.class);
        }

        public Map<String, Object> getDataJsonMap()
        {
            return getDataJson(new TypeReference<Map<String, Object>>() {});
        }
    }

    Response request(String host, int port, Request request);
}
