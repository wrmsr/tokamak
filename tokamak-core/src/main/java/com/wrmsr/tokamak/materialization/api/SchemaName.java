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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.wrmsr.tokamak.util.Box;

import javax.annotation.concurrent.Immutable;

import static com.wrmsr.tokamak.util.StringPrefixing.stripPrefix;

@Immutable
public final class SchemaName
        extends Box<String>
{
    public SchemaName(String value)
    {
        super(value);
    }

    public static String PREFIX = "schema:";

    public static SchemaName of(String value)
    {
        return new SchemaName(value);
    }

    @JsonCreator
    public static SchemaName parsePrefixed(String string)
    {
        return new SchemaName(stripPrefix(PREFIX, string));
    }

    @JsonValue
    public String toPrefixedString()
    {
        return PREFIX + value;
    }
}
