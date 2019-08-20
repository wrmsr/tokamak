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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class ArrayListView<T>
        implements List<T>
{
    private final T[] array;
    private final int startInclusive;
    private final int endExclusive;

    public ArrayListView(T[] array, int offset, int length)
    {
        checkArgument(offset >= 0);
        checkArgument(length >= 0);
        checkArgument(offset + length <= array.length);
        this.array = checkNotNull(array);
        this.startInclusive = offset;
        this.endExclusive = offset + length;
    }

    public ArrayListView(T[] array)
    {
        this(array, 0, array.length);
    }

    @Override
    public int size()
    {
        return endExclusive - startInclusive;
    }

    @Override
    public boolean isEmpty()
    {
        return startInclusive == endExclusive;
    }

    @Override
    public boolean contains(Object o)
    {
        for (int i = startInclusive; i < endExclusive; ++i) {
            if (Objects.equals(o, array[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator()
    {
        return Arrays.stream(array, startInclusive, endExclusive).iterator();
    }

    @Override
    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] a)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (int i = startInclusive; i < endExclusive; ++i) {
            if (!c.contains(array[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index)
    {
        return array[startInclusive + index];
    }

    @Override
    public T set(int index, T element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o)
    {
        for (int i = startInclusive; i < endExclusive; ++i) {
            if (Objects.equals(o, array[i])) {
                return i - startInclusive;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o)
    {
        int ret = -1;
        for (int i = startInclusive; i < endExclusive; ++i) {
            if (Objects.equals(o, array[i])) {
                ret = i - startInclusive;
            }
        }
        return ret;
    }

    public static final class ListIteratorImpl<T>
            implements ListIterator<T>
    {
        private final T[] array;
        private final int startInclusive;
        private final int endExclusive;

        private int index;

        public ListIteratorImpl(T[] array, int offset, int length, int index)
        {
            checkArgument(offset >= 0);
            checkArgument(length >= 0);
            checkArgument(index >= 0);
            checkArgument(offset + length <= array.length);
            checkArgument(index < length);
            this.array = checkNotNull(array);
            this.startInclusive = offset;
            this.endExclusive = offset + length;
            this.index = index;
        }

        @Override
        public boolean hasNext()
        {
            return (startInclusive + index) < endExclusive;
        }

        @Override
        public T next()
        {
            checkState(hasNext());
            return array[startInclusive + ++index];
        }

        @Override
        public boolean hasPrevious()
        {
            return index > 0;
        }

        @Override
        public T previous()
        {
            checkState(hasPrevious());
            return array[startInclusive + --index];
        }

        @Override
        public int nextIndex()
        {
            return index;
        }

        @Override
        public int previousIndex()
        {
            return index - 1;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t)
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ListIterator<T> listIterator()
    {
        return new ListIteratorImpl<>(array, startInclusive, endExclusive - startInclusive, 0);
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        return new ListIteratorImpl<>(array, startInclusive, endExclusive - startInclusive, index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        return new ArrayListView<>(array, startInclusive + fromIndex, toIndex - fromIndex);
    }
}
