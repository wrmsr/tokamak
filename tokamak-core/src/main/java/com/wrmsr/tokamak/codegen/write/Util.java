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
package com.wrmsr.tokamak.codegen.write;

import java.util.List;

import static java.lang.Character.isISOControl;

final class Util
{
    private Util()
    {
    }

    static String join(String separator, List<String> parts)
    {
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            result.append(separator).append(parts.get(i));
        }
        return result.toString();
    }

    static String characterLiteralWithoutSingleQuotes(char c)
    {
        // see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
        switch (c) {
            case '\b':
                return "\\b"; /* \u0008: backspace (BS) */
            case '\t':
                return "\\t"; /* \u0009: horizontal tab (HT) */
            case '\n':
                return "\\n"; /* \u000a: linefeed (LF) */
            case '\f':
                return "\\f"; /* \u000c: form feed (FF) */
            case '\r':
                return "\\r"; /* \u000d: carriage return (CR) */
            case '\"':
                return "\"";  /* \u0022: double quote (") */
            case '\'':
                return "\\'"; /* \u0027: single quote (') */
            case '\\':
                return "\\\\";  /* \u005c: backslash (\) */
            default:
                return isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
        }
    }

    /**
     * Returns the string literal representing {@code value}, including wrapping double quotes.
     */
    static String stringLiteralWithDoubleQuotes(String value, String indent)
    {
        StringBuilder result = new StringBuilder(value.length() + 2);
        result.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // trivial case: single quote must not be escaped
            if (c == '\'') {
                result.append("'");
                continue;
            }
            // trivial case: double quotes must be escaped
            if (c == '\"') {
                result.append("\\\"");
                continue;
            }
            // default case: just let character literal do its work
            result.append(characterLiteralWithoutSingleQuotes(c));
            // need to append indent after linefeed?
            if (c == '\n' && i + 1 < value.length()) {
                result.append("\"\n").append(indent).append(indent).append("+ \"");
            }
        }
        result.append('"');
        return result.toString();
    }
}
