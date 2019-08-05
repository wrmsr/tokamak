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

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public final class DelimitedEscaper
{
    private final char delimiterChar;
    private final char quoteChar;
    private final char escapeChar;
    private final Set<Character> escapedChars;

    private final Set<Character> controlChars;
    private final String quoteString;

    public DelimitedEscaper(char delimiterChar, char quoteChar, char escapeChar, Set<Character> escapedChars)
    {
        checkArgument(delimiterChar != quoteChar);
        checkArgument(delimiterChar != escapeChar);
        checkArgument(quoteChar != escapeChar);

        this.delimiterChar = delimiterChar;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.escapedChars = ImmutableSet.copyOf(escapedChars);

        controlChars = ImmutableSet.<Character>builder()
                .add(delimiterChar)
                .add(quoteChar)
                .add(escapeChar)
                .addAll(escapedChars)
                .build();

        quoteString = new String(new char[] {quoteChar});
    }

    public String escape(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (controlChars.contains(c)) {
                sb.append(escapeChar);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public String unescape(String str)
    {
        StringBuilder sb = new StringBuilder();
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] == escapeChar) {
                checkArgument(i <= arr.length - 2);
                sb.append(arr[++i]);
            }
            else {
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    // public String escapeQuoted(String str)
    // {
    //     if (str.contains(new String(new char[] {quoteChar}))) {
    //         return quoteChar + escape(str, escapeChar, ImmutableSet.of(quoteChar)) + quoteChar;
    //     }
    //     else {
    //         return str;
    //     }
    // }
    //
    // public static String unescapeQuoted(String str, char escapeChar, char quoteChar)
    // {
    //     if (str.startsWith(new String(new char[] {quoteChar}))) {
    //         checkArgument(str.endsWith(new String(new char[] {quoteChar})));
    //         return unescape(str.substring(1, str.length() - 1), escapeChar);
    //     }
    //     else {
    //         return
    //     }
    // }
}
