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
package com.wrmsr.tokamak.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.util.Json;

import javax.xml.bind.DatatypeConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public final class HttpClient
{
    private final URL baseUrl;
    private final String encodedAuthorization;

    public HttpClient(String hostname, int port)
    {
        this(hostname, port, null, null);
    }

    public HttpClient(String hostname, int port, String username, String password)
    {
        try {
            baseUrl = new URL("http", hostname, port, "/");
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (username != null) {
            String userPassword = username + ":" + password;
            encodedAuthorization = DatatypeConverter.printBase64Binary(userPassword.getBytes());
        }
        else {
            encodedAuthorization = null;
        }
    }

    public HttpClientResponse request(String path)
    {
        return request("GET", path, (byte[]) null);
    }

    public HttpClientResponse request(String method, String path)
    {
        return request(method, path, (byte[]) null);
    }

    public HttpURLConnection connect(String method, String path, boolean setDoOutput)
    {
        URL url;
        try {
            url = new URL(baseUrl, path);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            if (setDoOutput) {
                urlConnection.setDoOutput(true);
            }
            if (encodedAuthorization != null) {
                urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            }

            urlConnection.connect();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return urlConnection;
    }

    public HttpClientResponse request(String method, String path, byte[] data)
    {
        HttpURLConnection urlConnection = connect(method, path, data != null);

        try {
            if (data != null) {
                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(data);
                }
            }

            int responseCode = -1;
            try {
                responseCode = urlConnection.getResponseCode();
                InputStream inputStream = urlConnection.getInputStream();
                Map<String, Object> response = Json.OBJECT_MAPPER_SUPPLIER.get().readValue(inputStream, new TypeReference<Map<String, Object>>() {});
                return new HttpClientResponse(response, responseCode, null);
            }
            catch (Exception e) {
                InputStream errStream = urlConnection.getErrorStream();
                String body = CharStreams.toString(new InputStreamReader(errStream));
                return new HttpClientResponse(ImmutableMap.of("body", body), responseCode, e);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            urlConnection.disconnect();
        }
    }

    public HttpClientResponse request(String method, String path, Map<String, Object> data)
    {
        return request(method, path, Json.writeValue(data).getBytes(Charsets.UTF_8));
    }

    public HttpClientResponse requestRetrying(String method, String path, long millis, long sleepMillis)
    {
        long startTime = System.currentTimeMillis();
        while (true) {
            HttpClientResponse response = request(method, path);
            if (response.isSuccess()) {
                return response;
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= millis) {
                throw new IllegalStateException();
            }
            try {
                Thread.sleep(sleepMillis);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public HttpClientResponse requestRetrying(String path, long millis, long sleepMillis)
    {
        return requestRetrying("GET", path, millis, sleepMillis);
    }

    public HttpClientResponse requestRetrying(String path, long millis)
    {
        return requestRetrying(path, millis, 100);
    }
}
