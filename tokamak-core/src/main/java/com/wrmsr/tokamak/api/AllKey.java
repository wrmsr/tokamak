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
package com.wrmsr.tokamak.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class AllKey
        implements Key
{
    public static final AllKey INSTANCE = new AllKey();

    @JsonCreator
    public AllKey()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        return true;
    }

    @Override
    public int hashCode()
    {
        return 31;
    }

    @Override
    public int compareTo(Key o)
    {
        if (o instanceof AllKey) {
            return 0;
        }
        else {
            return 1;
        }
    }
}
