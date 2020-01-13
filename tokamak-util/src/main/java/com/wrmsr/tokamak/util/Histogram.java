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
import com.google.common.collect.Streams;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.LongPredicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public final class Histogram
{
    /*
    TODO:
     - 46 day rollover lols
     - reservoir
     - windowing
    */

    public static final List<Float> DEFAULT_PERCENTILES = ImmutableList.of(0.5f, 0.75f, 0.9f, 0.95f, 0.99f);

    @Immutable
    public static final class Percentile
    {
        private final float p;
        private final float value;

        private Percentile(float p, float value)
        {
            this.p = p;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return "Percentile{" +
                    "p=" + p +
                    ", value=" + value +
                    '}';
        }

        public float getP()
        {
            return p;
        }

        public float getValue()
        {
            return value;
        }
    }

    @Immutable
    public static final class Stats
    {
        private final int count;
        private final float min;
        private final float max;
        private final List<Percentile> lastPercentiles;
        private final List<Percentile> samplePercentiles;

        private Stats(
                int count,
                float min,
                float max,
                List<Percentile> lastPercentiles,
                List<Percentile> samplePercentiles)
        {
            this.count = count;
            this.min = min;
            this.max = max;
            this.lastPercentiles = lastPercentiles;
            this.samplePercentiles = samplePercentiles;
        }

        @Override
        public String toString()
        {
            return "Stats{" +
                    "count=" + count +
                    ", min=" + min +
                    ", max=" + max +
                    ", lastPercentiles=" + lastPercentiles +
                    ", samplePercentiles=" + samplePercentiles +
                    '}';
        }

        public int getCount()
        {
            return count;
        }

        public float getMin()
        {
            return min;
        }

        public float getMax()
        {
            return max;
        }

        public List<Percentile> getLastPercentiles()
        {
            return lastPercentiles;
        }

        public List<Percentile> getSamplePercentiles()
        {
            return samplePercentiles;
        }
    }

    private final int size;
    private final List<Float> percentiles;

    private volatile int count;
    private volatile float min = Float.POSITIVE_INFINITY;
    private volatile float max = Float.NEGATIVE_INFINITY;

    private final long baseMillis;

    private final List<Integer> percentilePosList;

    private final AtomicLongArray ring;
    private volatile int ringPos;

    private final AtomicLongArray sample;
    private volatile @Nullable AtomicInteger nextSamplePos = new AtomicInteger();

    public Histogram(int size, List<Float> percentiles)
    {
        checkArgument(size >= 1);
        this.size = size;
        this.percentiles = ImmutableList.copyOf(percentiles);
        this.percentiles.forEach(p -> checkArgument(p >= 0.0f));

        baseMillis = System.currentTimeMillis() - 1;

        percentilePosList = calcPercentilePosList(size);

        ring = new AtomicLongArray(size);
        sample = new AtomicLongArray(size);
    }

    public Histogram(int size)
    {
        this(size, DEFAULT_PERCENTILES);
    }

    public int getSize()
    {
        return size;
    }

    public List<Float> getPercentiles()
    {
        return percentiles;
    }

    private long makeEntry(long timestamp, float value)
    {
        return ((timestamp - baseMillis) << 32) | Float.floatToIntBits(value);
    }

    private long getEntryTimestamp(long entry)
    {
        return baseMillis + (entry >>> 32);
    }

    private static float getEntryValue(long entry)
    {
        return Float.intBitsToFloat((int) entry);
    }

    public void add(float value)
    {
        count += 1;
        min = Float.min(min, value);
        max = Float.max(max, value);

        long entry = makeEntry(System.currentTimeMillis(), value);

        ring.set(ringPos, entry);
        int nextRingPos = ringPos + 1;
        ringPos = nextRingPos >= size ? 0 : nextRingPos;

        int samplePos = -1;
        @Nullable AtomicInteger nextSamplePos = this.nextSamplePos;
        if (nextSamplePos != null) {
            samplePos = nextSamplePos.getAndIncrement();
            if (samplePos >= size) {
                this.nextSamplePos = null;
                samplePos = -1;
            }
        }
        if (samplePos < 0) {
            samplePos = ThreadLocalRandom.current().nextInt(0, size);
        }
        sample.set(samplePos, entry);
    }

    private static int calcPercentilePos(float p, int sz)
    {
        return Math.round((p * sz) - 1);
    }

    private List<Integer> calcPercentilePosList(int size)
    {
        return immutableMapItems(percentiles, p -> calcPercentilePos(p, size));
    }

    private List<Percentile> calcPercentiles(AtomicLongArray entries, LongPredicate entryPredicate)
    {
        float[] values = new float[entries.length()];
        int size = 0;
        for (int i = 0; i < values.length; ++i) {
            long entry = entries.get(i);
            if (entry != 0 && entryPredicate.test(entry)) {
                values[size++] = getEntryValue(entry);
            }
        }

        if (size < 1) {
            return ImmutableList.of();
        }

        List<Integer> posList = size == this.size ? percentilePosList : calcPercentilePosList(size);
        Arrays.sort(values, 0, size);
        return Streams.zip(percentiles.stream(), posList.stream(), (p, pos) -> new Percentile(p, values[pos])).collect(toImmutableList());
    }

    private Stats get(LongPredicate entryPredicate)
    {
        return new Stats(
                count,
                min,
                max,
                calcPercentiles(ring, entryPredicate),
                calcPercentiles(sample, entryPredicate));
    }

    public Stats get()
    {
        return get(e -> true);
    }

    public Stats getSince(long timestamp)
    {
        return get(e -> getEntryTimestamp(e) >= timestamp);
    }
}
