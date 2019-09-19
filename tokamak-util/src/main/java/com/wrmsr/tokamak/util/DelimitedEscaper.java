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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;

public final class DelimitedEscaper
{
    private final char delimitChar;
    private final char quoteChar;
    private final char escapeChar;
    private final Set<Character> escapedChars;

    private final Set<Character> allEscapedChars;

    public DelimitedEscaper(char delimitChar, char quoteChar, char escapeChar, Set<Character> escapedChars)
    {
        checkArgument(delimitChar != quoteChar);
        checkArgument(delimitChar != escapeChar);
        checkArgument(quoteChar != escapeChar);

        this.delimitChar = delimitChar;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.escapedChars = ImmutableSet.copyOf(escapedChars);

        allEscapedChars = ImmutableSet.<Character>builder()
                .add(delimitChar)
                .add(quoteChar)
                .add(escapeChar)
                .addAll(escapedChars)
                .build();
    }

    @Override
    public String toString()
    {
        return "DelimitedEscaper{" +
                "delimitChar=" + delimitChar +
                ", quoteChar=" + quoteChar +
                ", escapeChar=" + escapeChar +
                ", escapedChars=" + escapedChars +
                '}';
    }

    public char getDelimitChar()
    {
        return delimitChar;
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
        return c == delimitChar || c == quoteChar || c == escapeChar;
    }

    public boolean containsEscapedChar(String str)
    {
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (allEscapedChars.contains(c)) {
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
                checkArgument(!allEscapedChars.contains(c));
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

    public String delimit(String... strs)
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String str : strs) {
            if (count++ > 0) {
                sb.append(delimitChar);
            }
            if (containsEscapedChar(str)) {
                sb.append(quote(str));
            }
            else {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    public String delimit(Iterable<String> strs)
    {
        return delimit(StreamSupport.stream(strs.spliterator(), false).toArray(String[]::new));
    }

    public List<String> undelimit(String str)
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int i = 0;

        while (i < str.length()) {
            char c = str.charAt(i);

            if (count > 0) {
                checkArgument(c == delimitChar);
                checkArgument(i < str.length() - 1);
                c = str.charAt(++i);
            }

            boolean quoted = c == quoteChar;
            if (quoted) {
                checkArgument(i < str.length() - 1);
                c = str.charAt(++i);
            }
            boolean unquoted = false;

            while (true) {
                if (c == delimitChar) {
                    if (!quoted) {
                        break;
                    }
                    else {
                        sb.append(c);
                    }
                }
                else if (c == quoteChar) {
                    checkArgument(quoted);
                    unquoted = true;
                    i++;
                    break;
                }
                else if (c == escapeChar) {
                    checkArgument(quoted);
                    checkArgument(i <= str.length() - 2);
                    sb.append(str.charAt(++i));
                }
                else {
                    checkArgument(!escapedChars.contains(c));
                    sb.append(c);
                }

                if (++i == str.length()) {
                    break;
                }
                c = str.charAt(i);
            }

            if (quoted) {
                checkArgument(unquoted);
            }

            builder.add(sb.toString());
            sb = new StringBuilder();
            count++;
        }

        return builder.build();
    }
}
