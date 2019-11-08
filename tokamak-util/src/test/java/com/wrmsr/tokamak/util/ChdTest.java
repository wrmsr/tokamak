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
import java.util.function.LongSupplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChdTest
        extends TestCase
{
    /*
    https://github.com/alecthomas/mph
    */

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
            long i = Long.remainderUnsigned(h, indices.length);
            long ri = indices[(int) i];
            if (ri >= r.length) {
                return null;
            }
            long r = this.r[(int) ri];
            long ti = Long.remainderUnsigned(h ^ r, keys.length);
            byte[] k = keys[(int) ti];
            if (!Arrays.equals(k, key)) {
                return null;
            }
            return values[(int) ti];
        }
    }

    public static final class Hasher
    {
        private int size;
        private int buckets;
        private final LongSupplier rand;

        private List<Long> r = new ArrayList<>();

        public Hasher(int size, int buckets, LongSupplier rand)
        {
            this.size = size;
            this.buckets = buckets;
            this.rand = rand;
            r.add(rand.getAsLong());
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

        public int hashIndexFromKey(byte[] b)
        {
            long v = Long.remainderUnsigned(hash(b) ^ r.get(0), buckets);
            return (int) (v > 0 ? v : -v) & Integer.MAX_VALUE;
        }

        public int table(long r, byte[] b)
        {
            long v = Long.remainderUnsigned(hash(b) ^ this.r.get(0) ^ r, size);
            return (int) (v > 0 ? v : -v) & Integer.MAX_VALUE;
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
        private final LongSupplier rand;

        private List<byte[]> keys = new ArrayList<>();
        private List<byte[]> values = new ArrayList<>();

        public Builder(LongSupplier rand)
        {
            this.rand = rand;
        }

        public Builder()
        {
            this(ThreadLocalRandom.current()::nextLong);
        }

        public void put(byte[] key, byte[] value)
        {
            keys.add(key);
            values.add(value);
        }

        public Hash build()
        {
            int n = this.keys.size();
            int m = n / 2;
            if (m == 0) {
                m = 1;
            }

            byte[][] keys = new byte[n][];
            byte[][] values = new byte[n][];

            Hasher hasher = new Hasher(n, m, rand);

            Bucket[] buckets = new Bucket[m];
            for (int i = 0; i < buckets.length; ++i) {
                buckets[i] = new Bucket();
            }
            int[] indices = new int[m];
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
                int oh = hasher.hashIndexFromKey(key);

                Bucket bucket = buckets[oh];
                bucket.index = oh;
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
                    long r = rand.getAsLong();
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

    private static final long[] RANDS = new long[] {
            0x78fc2ffac2fd9401L,
            0x1f5b0412ffd341c0L,
            0x53f65ff94f6ec873L,
            0x86f4bd2ae8eea562L,
            0xaf0d18fb750b2d4aL,
            0xa50db1e09ea748afL,
            0x18a1d40f85514639L,
            0xd3e1ec85e22e8978L,
            0xf2d6750878551451L,
            0x156bded0d0d3e24eL,
            0xa4f05fe84cb4f9f8L,
            0xf3888e3bf8b1c644L,
            0x6cc599ab7a85bf3bL,
            0x22f3329c42c752b2L,
            0x4df656f89eb7aea8L,
            0xa077ccce0d8fc159L,
            0xe327debf817a5ec7L,
            0x23ccf42e2cf675fL,
            0xe4bed6e2edf354c3L,
            0xe652885eaea34eccL,
            0x2d9ecb8b574a9f6aL,
            0xb68d761453a6d4f2L,
            0xc14f9eea6197296dL,
            0x9ec678fcc3aea65aL,
            0x9ac72081ac81e0aaL,
            0xd8b684ea6ccdef20L,
            0x337163e07b605e92L,
            0xa4751dd0cddd966fL,
            0x57798a0f003f5c04L,
            0x3801382c516cce6bL,
            0x8d505baddfeecaaaL,
            0x2871a7e4c0e86466L,
            0x135d96c1b7b8e0ecL,
            0xdd9c9c7c1b258191L,
            0xa836a216fc5a20a5L,
            0x2f2a2dd1d2cdefa2L,
            0xd2e90a28a874d079L,
            0x2e08caaed6b09e43L,
            0xc56a867dd602ae23L,
            0x25350724afec4c2L,
            0xc251514f9d9b11daL,
            0xeb4409c23d7a240L,
            0x60c41f940d95c35aL,
            0xcd22d36ab90c1cfeL,
            0x6ce1bf5f298222d6L,
            0x8ab56d0733a4261eL,
            0xf62ad3343a4c44c1L,
            0x7881d1e8fb7f4a5cL,
            0xbc4f90fe8c3bedf7L,
            0xead9bc296df0f893L,
            0x8564042e187b84edL,
            0xe8f0f3b0c44bf410L,
            0xb6f2300a82060d3aL,
            0x7b8a67304111f857L,
            0x434c9e3c18645c0L,
            0x65d866c455808b2cL,
            0xcad60699251d4486L,
            0x6ce98a964b89cd9aL,
            0x5a4e65c969debf0L,
            0xc2011588be4e3c69L,
            0xd102eb666b84d9b7L,
            0xc1ba6c7bda5c7eb5L,
            0x1f6c68bd16d69168L,
            0xebac83a6134b837L,
            0x648368fe8f002ca2L,
            0x6b21f1e3e44a7352L,
            0xb9140827835fcd7aL,
            0x251167d0b5671830L,
            0xb0b257791c0038b2L,
            0x76df88313fce1977L,
            0xbf59826fbfee7de5L,
            0x10a02c56bbf7105aL,
            0x61582994273d5c4dL,
            0xb74906676232dbc6L,
            0x9d6731a2d997bcf3L,
            0xa3e6dfa582e6ed35L,
            0xf5d08ac9fb11a0f1L,
            0x1ae8013c0090e7fbL,
            0xf05e66af037796e9L,
            0x67d4034b7f40729fL,
            0x540d8afeaa74b4fdL,
            0xefcf5046dd15053eL,
            0xbbcb8b24812b1751L,
            0xea5b6c0b409e967fL,
            0x62fa12c4b1687712L,
            0x7037cf3176f58ce9L,
            0x7e7dba4a4b3b03L,
            0xf6c9497214ba19d3L,
            0x9bda06c4d170ac08L,
            0xf20dbab68e820edeL,
            0x3b103e548582a8caL,
            0x253b60c83f643c21L,
            0xbbbcba7066236058L,
            0xb00e19c4f4d70badL,
            0xd1ead168a8233632L,
            0x7d3166a2409f76e1L,
            0xb2055621d53b1b43L,
            0x9cac99d4ea6f08d2L,
            0xae3d28123d65a463L,
            0x8a8aa995379c0156L,
    };

    public void testHash()
    {
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("one", "1")
                .put("two", "2")
                .put("three", "3")
                .put("four", "4")
                .put("five", "5")
                .put("six", "6")
                .put("seven", "7")
                .build();

        Builder b = new Builder(
                // new Random(0)::nextLong
                Arrays.stream(RANDS).iterator()::next
        );
        map.forEach((k, v) -> b.put(utf8(k), utf8(v)));

        Hash h = b.build();
        map.forEach((k, v) -> {
            System.out.println(Arrays.toString(h.get(utf8(k))));
            System.out.println(Arrays.toString(h.get(utf8(k + "f"))));
        });
    }
}
