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
package com.wrmsr.tokamak.core.search.node;

import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.UnaryOperator.identity;

public enum SCmp
{
    EQ("=="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<=");

    private final String string;

    SCmp(String string)
    {
        this.string = checkNotEmpty(string);
    }

    public String getString()
    {
        return string;
    }

    public static final Map<String, SCmp> STRING_MAP = Arrays.stream(SCmp.values()).collect(toImmutableMap(SCmp::getString, identity()));

    public static SCmp fromString(String str)
    {
        return checkNotNull(STRING_MAP.get(checkNotEmpty(str)));
    }
}
