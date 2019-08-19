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
package com.wrmsr.tokamak.codec;

import java.util.Map;

public interface RowCodec
{
    /*
    TODO:
     - write to a bytebuffer not join arrs
      - on variable prealloc size byte
     - operate on object[] not maps
     - key awareness
     - length awareness
     - packed
     - salting
     - vints
    */

    void encode(Map<String, Object> row, Output output);

    @FunctionalInterface
    interface Sink
    {
        void put(String field, Object value);
    }

    void decode(Sink sink, Input input);
}
