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

import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Map;

public interface RowIdCodec
        extends Codec<Map<String, Object>, byte[]>
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
}
