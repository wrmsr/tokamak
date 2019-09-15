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

import com.google.common.io.ByteStreams;

import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JdkHttpClient
        implements HttpClient
{
    @Override
    public Response request(String host, int port, Request request)
    {
        URL url;
        try {
            url = new URL("http", host, port, "/" + request.getPath());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(request.getMethod());
            if (request.getData() != null) {
                conn.setDoOutput(true);
            }
            if (request.getAuth() != null) {
                conn.setRequestProperty("Authorization", "Basic " + request.getAuth().getEncoded());
            }
            request.getHeaders().forEach(conn::setRequestProperty);

            conn.connect();
            try {
                if (request.getData() != null) {
                    try (OutputStream outputStream = conn.getOutputStream()) {
                        outputStream.write(request.getData());
                    }
                }

                int code = conn.getResponseCode();
                InputStream inputStream = conn.getInputStream();
                byte[] responseData = ByteStreams.toByteArray(inputStream);
                return new Response(code, responseData);
            }
            finally {
                conn.disconnect();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
