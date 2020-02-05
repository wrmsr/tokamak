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
package com.wrmsr.tokamak.util.match.pattern;

import com.wrmsr.tokamak.util.match.Capture;
import com.wrmsr.tokamak.util.match.Captures;
import com.wrmsr.tokamak.util.match.Match;
import com.wrmsr.tokamak.util.match.pattern.matcher.PatternMatcher;
import com.wrmsr.tokamak.util.match.pattern.visitor.PatternVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CapturePattern<T>
        extends Pattern<T>
{
    private final Capture<T> capture;

    public CapturePattern(Capture<T> capture, Pattern<?> next)
    {
        super(next);
        this.capture = checkNotNull(capture);
    }

    public Capture<T> getCapture()
    {
        return capture;
    }

    @Override
    public <R, C> R accept(PatternVisitor<R, C> visitor, C context)
    {
        return visitor.visitCapture(this, context);
    }

    @Override
    public Match<T> accept(PatternMatcher matcher, Object object, Captures captures)
    {
        return matcher.matchCapture(this, object, captures);
    }
}
