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
package com.wrmsr.util;

import java.util.Map;

public final class HttpClientResponse
{
    private final Map<String, Object> response;

    private final int responseCode;

    private final Throwable e;

    public HttpClientResponse(Map<String, Object> response, int responseCode, Throwable e)
    {
        this.response = response;
        this.responseCode = responseCode;
        this.e = e;
    }

    public Map<String, Object> response()
    {
        return response;
    }

    public Object get(String key)
    {
        return response().get(key);
    }

    @SuppressWarnings({"unchecked"})
    public String getString(String key)
    {
        String[] keys = key.split("\\.");
        Map<String, Object> map = response();
        for (int i = 0; i < keys.length - 1; i++) {
            map = (Map<String, Object>) map.get(keys[i]);
            if (map == null) {
                return null;
            }
        }

        return (String) map.get(keys[keys.length - 1]);
    }

    public int responseCode()
    {
        return responseCode;
    }

    public boolean isSuccess()
    {
        return responseCode >= 200 && responseCode() < 300;
    }

    public Throwable cause()
    {
        return e;
    }
}
