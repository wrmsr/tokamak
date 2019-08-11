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
package com.wrmsr.tokamak.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.misc.Interval;

import static com.google.common.base.Preconditions.checkNotNull;

public class CaseInsensitiveCharStream
        implements CharStream
{
    private final CharStream child;

    public CaseInsensitiveCharStream(CharStream child)
    {
        this.child = checkNotNull(child);
    }

    @Override
    public String getText(Interval interval)
    {
        return child.getText(interval);
    }

    @Override
    public void consume()
    {
        child.consume();
    }

    @Override
    public int LA(int i)
    {
        int result = child.LA(i);

        switch (result) {
            case 0:
            case IntStream.EOF:
                return result;
            default:
                return Character.toUpperCase(result);
        }
    }

    @Override
    public int mark()
    {
        return child.mark();
    }

    @Override
    public void release(int marker)
    {
        child.release(marker);
    }

    @Override
    public int index()
    {
        return child.index();
    }

    @Override
    public void seek(int index)
    {
        child.seek(index);
    }

    @Override
    public int size()
    {
        return child.size();
    }

    @Override
    public String getSourceName()
    {
        return child.getSourceName();
    }
}
