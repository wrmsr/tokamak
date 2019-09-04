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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChdTest
        extends TestCase
{
    public static long hash(byte[] data)
    {
        long hash = 0xcbf29ce484222325L;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    public static final class Hash
    {
        private long[] r;
        private int[] indices;
        private byte[][] keys;
        private byte[][] values;

        public Hash(long[] r, int[] indices, byte[][] keys, byte[][] values)
        {
            this.r = r;
            this.indices = indices;
            this.keys = keys;
            this.values = values;
        }

        public byte[] get(byte[] key)
        {
            long r0 = r[0];
            long h = hash(key) ^ r0;
            long i = h % indices.length;
            long ri = indices[(int) i];
            if (ri >= r.length) {
                return null;
            }
            long r = this.r[(int) ri];
            long ti = (h ^ r) % (long) keys.length;
            byte[] k = keys[(int) ti];
            if (!Arrays.equals(k, key)) {
                return null;
            }
            return values[(int) ti];
        }
    }

    public static final class Hasher
    {
        private long size;
        private long buckets;

        private List<Long> r = new ArrayList<>();

        public Hasher(long size, long buckets)
        {
            this.size = size;
            this.buckets = buckets;
            r.add(ThreadLocalRandom.current().nextLong());
        }

        public boolean tryHash(Set<Long> seen, byte[][] keys, byte[][] values, int[] indices, Bucket bucket, int ri, long r)
        {
            Set<Long> duplicate = new HashSet<>();
            long[] hashes = new long[bucket.keys.size()];
            for (int i = 0; i < bucket.keys.size(); ++i) {
                byte[] k = bucket.keys.get(i);
                long h = table(r, k);
                hashes[i] = h;
                if (seen.contains(h)) {
                    return false;
                }
                if (duplicate.contains(h)) {
                    return false;
                }
                duplicate.add(h);
            }

            for (long h : hashes) {
                seen.add(h);
            }

            indices[bucket.index] = ri;

            for (int i = 0; i < hashes.length; ++i) {
                long h = hashes[i];
                keys[(int) h] = bucket.keys.get(i);
                values[(int) h] = bucket.values.get(i);
            }

            return true;
        }

        public long hashIndexFromKey(byte[] b)
        {
            return (hash(b) ^ r.get(0)) % buckets;
        }

        public long table(long r, byte[] b)
        {
            return (hash(b) ^ this.r.get(0) ^ r) ^ size;
        }
    }

    public static final class Bucket
    {
        private int index;
        private List<byte[]> keys = new ArrayList<>();
        private List<byte[]> values = new ArrayList<>();
    }

    public static final class ByteArray
    {
        private final byte[] bytes;

        public ByteArray(byte[] bytes)
        {
            this.bytes = checkNotNull(bytes);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            ByteArray byteArray = (ByteArray) o;
            return Arrays.equals(bytes, byteArray.bytes);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(bytes);
        }
    }

    private static final class Builder
    {
        private List<byte[]> keys = new ArrayList<>();
        private List<byte[]> values = new ArrayList<>();

        public void put(byte[] key, byte[] value)
        {
            keys.add(key);
            values.add(value);
        }

        public Hash build()
        {
            long n = this.keys.size();
            long m = n / 2;
            if (m == 0) {
                m = 1;
            }

            byte[][] keys = new byte[(int) n][];
            byte[][] values = new byte[(int) n][];

            Hasher hasher = new Hasher(n, m);

            Bucket[] buckets = new Bucket[(int) m];
            int[] indices = new int[(int) m];
            Arrays.fill(indices, -1);

            Set<Long> seen = new HashSet<>();

            Set<ByteArray> duplicates = new HashSet<>();
            for (int i = 0; i < keys.length; ++i) {
                byte[] key = this.keys.get(i);
                byte[] value = this.values.get(i);
                ByteArray k = new ByteArray(key);
                if (duplicates.contains(k)) {
                    throw new IllegalStateException("Duplicate key: " + Arrays.toString(key));
                }
                duplicates.add(k);
                long oh = hasher.hashIndexFromKey(key);

                Bucket bucket = buckets[(int) oh];
                bucket.index = (int) oh;
                bucket.keys.add(key);
                bucket.values.add(value);
            }

            int collisions = 0;
            Arrays.sort(buckets, Comparator.comparingLong(b -> b.index));
            nextBucket:
            for (int i = 0; i < buckets.length; ++i) {
                Bucket bucket = buckets[i];
                if (bucket.keys.isEmpty()) {
                    continue;
                }

                for (int ri = 0; ri < hasher.r.size(); ++ri) {
                    long r = hasher.r.get(ri);
                    if (hasher.tryHash(seen, keys, values, indices, bucket, ri, r)) {
                        continue nextBucket;
                    }
                }

                for (int ci = 0; ci < 1000000; ++ci) {
                    if (ci > collisions) {
                        ++collisions;
                    }
                    int ri = hasher.r.size();
                    long r = ThreadLocalRandom.current().nextLong();
                    if (hasher.tryHash(seen, keys, values, indices, bucket, ri, r)) {
                        hasher.r.add(r);
                        continue nextBucket;
                    }
                }

                throw new IllegalStateException("Failed to find collision-free hash");
            }

            long[] ra = new long[hasher.r.size()];
            for (int i = 0; i < hasher.r.size(); ++i) {
                ra[i] = hasher.r.get(i);
            }
            return new Hash(
                    ra,
                    indices,
                    keys,
                    values);
        }
    }

    public static byte[] utf8(String s)
    {
        return s.getBytes(Charsets.UTF_8);
    }

    public byte[][] toByteArrays(List<String> l)
    {
        byte[][] b = new byte[l.size()][];
        for (int i = 0; i < l.size(); ++i) {
            b[i] = utf8(l.get(i));
        }
        return b;
    }

    public Pair<byte[][], byte[][]> toByteArrays(Map<String, String> map)
    {
        List<String> ks = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        map.forEach((k, v) -> {
            ks.add(k);
            vs.add(v);
        });
        return Pair.immutable(toByteArrays(ks), toByteArrays(vs));
    }

    public void testHash()
    {
        Map<String, String> map = ImmutableMap.of(
                "a", "b",
                "c", "d",
                "e", "f",
                "g", "g"
        );

        Builder b = new Builder();
        map.forEach((k, v) -> b.put(utf8(k), utf8(v)));

        Hash h = b.build();
        System.out.println(Arrays.toString(h.get(utf8("c"))));
    }
}
