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
package com.wrmsr.tokamak.util;

import com.wrmsr.tokamak.util.io.HttpClient;
import com.wrmsr.tokamak.util.io.JdkHttpClient;
import org.junit.Test;

public class HttpTest
{
    @Test
    public void testHttp()
            throws Exception
    {
        HttpClient httpClient = new JdkHttpClient();
        HttpClient.Response resp = httpClient.request("www.google.com", 80, HttpClient.Request.of("GET", "/robots.txt"));
        System.out.println(resp);
    }
}
