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

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public final class DelimitedEscaper
{
    private final char delimiterChar;
    private final char quoteChar;
    private final char escapeChar;
    private final Set<Character> escapedChars;

    private final Set<Character> allEscapedChars;

    public DelimitedEscaper(char delimiterChar, char quoteChar, char escapeChar, Set<Character> escapedChars)
    {
        checkArgument(delimiterChar != quoteChar);
        checkArgument(delimiterChar != escapeChar);
        checkArgument(quoteChar != escapeChar);

        this.delimiterChar = delimiterChar;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.escapedChars = ImmutableSet.copyOf(escapedChars);

        allEscapedChars = ImmutableSet.<Character>builder()
                .add(delimiterChar)
                .add(quoteChar)
                .add(escapeChar)
                .addAll(escapedChars)
                .build();
    }

    @Override
    public String toString()
    {
        return "DelimitedEscaper{" +
                "delimiterChar=" + delimiterChar +
                ", quoteChar=" + quoteChar +
                ", escapeChar=" + escapeChar +
                ", escapedChars=" + escapedChars +
                '}';
    }

    public char getDelimiterChar()
    {
        return delimiterChar;
    }

    public char getQuoteChar()
    {
        return quoteChar;
    }

    public char getEscapeChar()
    {
        return escapeChar;
    }

    public Set<Character> getEscapedChars()
    {
        return escapedChars;
    }

    public boolean isControlChar(char c)
    {
        return c == delimiterChar || c == quoteChar || c == escapeChar;
    }

    public boolean containsControlChar(String str)
    {
        for (int i = 0; i < str.length(); ++i) {
            if (isControlChar(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEscapedChar(String str)
    {
        for (int i = 0; i < str.length(); ++i) {
            if (allEscapedChars.contains(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public String escape(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (allEscapedChars.contains(c)) {
                sb.append(escapeChar);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public String unescape(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == escapeChar) {
                checkArgument(i <= str.length() - 2);
                sb.append(str.charAt(++i));
            }
            else {
                checkArgument(!isControlChar(c));
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String quote(String str)
    {
        if (containsEscapedChar(str)) {
            return quoteChar + escape(str) + quoteChar;
        }
        else {
            return str;
        }
    }

    public String unquote(String str)
    {
        if (!str.isEmpty() && str.charAt(0) == quoteChar) {
            checkArgument(str.length() > 1);
            checkArgument(str.charAt(str.length() - 1) == quoteChar);
            return unescape(str.substring(1, str.length() - 1));
        }
        else {
            return str;
        }
    }

    public String delimit(Iterable<String> strs)
    {

    }

    public List<String> undelimit(String str)
    {
       
    }
}
