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
package com.wrmsr.tokamak.util.match;

public final class Captures
{
    private static final Captures NIL = new Captures(null, null, null);

    private final Capture<?> capture;
    private final Object value;
    private final Captures next;

    private Captures(Capture<?> capture, Object value, Captures next)
    {
        this.capture = capture;
        this.value = value;
        this.next = next;
    }

    public static Captures empty()
    {
        return NIL;
    }

    public static <T> Captures ofNullable(Capture<T> capture, T value)
    {
        return capture == null ? empty() : new Captures(capture, value, NIL);
    }

    public Captures addAll(Captures other)
    {
        if (this == NIL) {
            return other;
        }
        else {
            return new Captures(capture, value, next.addAll(other));
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T> T get(Capture<T> capture)
    {
        if (this == NIL) {
            throw new IllegalStateException();
        }
        else if (this.capture == capture) {
            return (T) value;
        }
        else {
            return next.get(capture);
        }
    }
}
