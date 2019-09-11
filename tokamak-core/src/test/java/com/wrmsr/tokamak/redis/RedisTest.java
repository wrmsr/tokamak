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
package com.wrmsr.tokamak.redis;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

public class RedisTest
        extends TestCase
{

    public void testRedis()
            throws Throwable
    {
        for (String str : new String[] {
                "+OK\r\n",
                "$6\r\nfoobar\r\n",
                "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n",
                "*3\r\n:1\r\n:2\r\n:3\r\n",
        }) {
            List<Object> lst = ImmutableList.copyOf(Resp.decode(new BufferedInputStream(new ByteArrayInputStream(str.getBytes()))));
            System.out.println(lst);
        }
    }
}
