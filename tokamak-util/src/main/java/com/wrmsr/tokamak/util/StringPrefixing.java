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
package com.wrmsr.tokamak.util;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StringPrefixing
{
    private StringPrefixing()
    {
    }

    public static class InvalidPrefixedStringException
            extends RuntimeException
    {
        private final String expectedPrefix;
        private final String value;

        public InvalidPrefixedStringException(String expectedPrefix, String value)
        {
            super(String.format("Expected prefix '%s', got value '%s'", expectedPrefix, value));
            this.expectedPrefix = expectedPrefix;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return "InvalidPrefixedStringException{" +
                    "expectedPrefix='" + expectedPrefix + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        public String getExpectedPrefix()
        {
            return expectedPrefix;
        }

        public String getValue()
        {
            return value;
        }
    }

    public static String stripPrefix(String expectedPrefix, String value)
    {
        checkNotNull(expectedPrefix);
        checkNotNull(value);
        if (!value.startsWith(expectedPrefix)) {
            throw new InvalidPrefixedStringException(expectedPrefix, value);
        }
        return value.substring(expectedPrefix.length());
    }
}
