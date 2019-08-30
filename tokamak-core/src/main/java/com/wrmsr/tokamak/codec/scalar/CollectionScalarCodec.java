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
package com.wrmsr.tokamak.codec.scalar;

import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.codec.Width;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MoreOptionals.mapOptional;

@Immutable
public abstract class CollectionScalarCodec<V>
        implements ScalarCodec<V>
{
    public static final int MAX_BYTE_SIZE = 255;
    public static final int DEFAULT_MAX_SIZE = MAX_BYTE_SIZE;

    protected final int size;
    protected final boolean fixed;

    protected final boolean byteSized;

    public CollectionScalarCodec(int size, boolean fixed)
    {
        checkArgument(size > 0);
        this.size = size;
        this.fixed = fixed;
        byteSized = size <= MAX_BYTE_SIZE;
    }

    public CollectionScalarCodec()
    {
        this(DEFAULT_MAX_SIZE, false);
    }

    public abstract Width getEntryWidth();

    private final SupplierLazyValue<Width> width = new SupplierLazyValue<>();

    @Override
    public Width getWidth()
    {
        return width.get(() -> {
            Width entryWidth = getEntryWidth();
            return Width.of(
                    mapOptional(entryWidth.getMin(), w -> fixed ? (w * size) : (byteSized ? 1 : 8)),
                    mapOptional(entryWidth.getMax(), w -> fixed ? (w * size) : ((byteSized ? 1 : 8) + (w * size))));
        });
    }

    protected void encodeSize(int sz, Output output)
    {
        if (fixed) {
            checkArgument(sz == size);
        }
        else if (byteSized) {
            checkArgument(sz < DEFAULT_MAX_SIZE);
            output.put((byte) sz);
        }
        else {
            output.putLong(sz);
        }
    }

    protected int decodeSize(Input input)
    {
        if (fixed) {
            return size;
        }
        else if (byteSized) {
            return input.get() & 0xFF;
        }
        else {
            return (int) input.getLong();
        }
    }
}
