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
import com.fasterxml.jackson.annotation.JsonValue;
import com.wrmsr.tokamak.util.Box;

import javax.annotation.concurrent.Immutable;

import static com.wrmsr.tokamak.util.StringPrefixing.stripPrefix;

@Immutable
public final class OutputName
        extends Box<String>
{
    public OutputName(String value)
    {
        super(value);
    }

    public static String PREFIX = "output:";

    public static OutputName of(String value)
    {
        return new OutputName(value);
    }

    @JsonCreator
    public static OutputName parsePrefixed(String string)
    {
        return new OutputName(stripPrefix(PREFIX, string));
    }

    @JsonValue
    public String toPrefixedString()
    {
        return PREFIX + value;
    }
}
