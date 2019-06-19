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
package com.wrmsr.tokamak.materialization.api;

import com.wrmsr.tokamak.util.LongBox;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Txid
        extends LongBox
{
    public Txid(long value)
    {
        super(value);
    }

    public static Txid of(long value)
    {
        return new Txid(value);
    }
}
