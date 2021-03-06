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
package com.wrmsr.tokamak.core.serde;

public interface Input
{
    byte get();

    long getLong();

    byte[] getBytes();

    default byte[] getBytes(int sz)
    {
        byte[] buf = new byte[sz];
        for (int i = 0; i < sz; ++i) {
            buf[i] = get();
        }
        return buf;
    }

    Input nest(int sz);
}
