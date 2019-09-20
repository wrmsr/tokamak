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
package com.wrmsr.tokamak.util.kv;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

@SuppressWarnings({"unchecked"})
public interface BatchedKv<K, V>
        extends Kv<K, V>
{
    abstract class BatchOperation<K, V>
            implements Map.Entry<K, V>
    {
        protected final K key;
        protected final V value;

        public BatchOperation(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return null;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }

    final class GetBatchOperation<K, V>
            extends BatchOperation<K, V>
    {
        public GetBatchOperation(K key)
        {
            super(key, null);
        }
    }

    final class ContainsKeyBatchOperation<K, V>
            extends BatchOperation<K, V>
    {
        public ContainsKeyBatchOperation(K key)
        {
            super(key, null);
        }
    }

    final class PutBatchOperation<K, V>
            extends BatchOperation<K, V>
    {
        public PutBatchOperation(K key, V value)
        {
            super(key, value);
        }

        public PutBatchOperation(Map.Entry<K, V> entry)
        {
            super(entry.getKey(), entry.getValue());
        }
    }

    final class RemoveBatchOperation<K, V>
            extends BatchOperation<K, V>
    {
        public RemoveBatchOperation(K key)
        {
            super(key, null);
        }
    }

    abstract class BatchResult<K, V>
            implements Map.Entry<K, V>
    {
        protected final K key;
        protected final V value;

        public BatchResult(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }

        public boolean isPresent()
        {
            return value != null;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }

    final class GetBatchResult<K, V>
            extends BatchResult<K, V>
    {
        public GetBatchResult(K key, V value)
        {
            super(key, value);
        }
    }

    final class ContainsKeyBatchResult<K, V>
            extends BatchResult<K, V>
    {
        protected final boolean isPresent;

        public ContainsKeyBatchResult(K key, boolean isPresent)
        {
            super(key, null);
            this.isPresent = isPresent;
        }

        @Override
        public V getValue()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPresent()
        {
            return isPresent;
        }
    }

    final class PutBatchResult<K, V>
            extends BatchResult<K, V>
    {
        public PutBatchResult(K key)
        {
            super(key, null);
        }
    }

    final class RemoveBatchResult<K, V>
            extends BatchResult<K, V>
    {
        public RemoveBatchResult(K key)
        {
            super(key, null);
        }
    }

    final class BatchBuilder<K, V>
    {
        private final ImmutableList.Builder<BatchOperation<K, V>> builder;

        public BatchBuilder()
        {
            builder = ImmutableList.builder();
        }

        public BatchBuilder<K, V> get(K key)
        {
            builder.add(new GetBatchOperation<>(key));
            return this;
        }

        public BatchBuilder<K, V> containsKey(K key)
        {
            builder.add(new ContainsKeyBatchOperation<>(key));
            return this;
        }

        public BatchBuilder<K, V> put(K key, V value)
        {
            builder.add(new PutBatchOperation<>(key, value));
            return this;
        }

        public BatchBuilder<K, V> remove(K key)
        {
            builder.add(new RemoveBatchOperation<>(key));
            return this;
        }

        public List<BatchOperation<K, V>> build()
        {
            return builder.build();
        }
    }

    static <K, V> BatchBuilder<K, V> batchBuilder()
    {
        return new BatchBuilder<>();
    }

    List<BatchResult<K, V>> batch(List<BatchOperation<K, V>> operations);

    default Map<K, V> getAll(List<? extends K> keys)
    {
        BatchBuilder<K, V> builder = batchBuilder();
        keys.forEach(builder::get);
        return batch(builder.build()).stream().collect(toImmutableMap());
    }

    default Set<K> containsKeys(Set<? extends K> keys)
    {
        BatchBuilder<K, V> builder = batchBuilder();
        keys.forEach(builder::containsKey);
        ImmutableSet.Builder<K> ret = ImmutableSet.builder();
        batch(builder.build()).stream()
                .map(ContainsKeyBatchResult.class::cast)
                .filter(ContainsKeyBatchResult::isPresent)
                .forEach(k -> ret.add((K) k));
        return ret.build();
    }

    default void putAll(Map<? extends K, ? extends V> map)
    {
        BatchBuilder<K, V> builder = batchBuilder();
        map.forEach(builder::put);
        batch(builder.build());
    }

    default void removeAll(Set<? extends K> keys)
    {
        BatchBuilder<K, V> builder = batchBuilder();
        keys.forEach(builder::remove);
        batch(builder.build());
    }

    @Override
    default V get(K key)
    {
        return batch(ImmutableList.of(new GetBatchOperation<>(key))).get(0).getValue();
    }

    @Override
    default void put(K key, V value)
    {
        batch(ImmutableList.of(new PutBatchOperation<>(key, value)));
    }

    @Override
    default void remove(K key)
    {
        batch(ImmutableList.of(new RemoveBatchOperation<>(key)));
    }

    final class FromKv<K, V>
            implements BatchedKv<K, V>
    {
        private final Kv<K, V> target;

        public FromKv(Kv<K, V> target)
        {
            this.target = target;
        }

        @Override
        public Iterator<K> iterator()
        {
            return target.iterator();
        }

        @Override
        public ManagedIterator<K> managedIterator()
        {
            return target.managedIterator();
        }

        @Override
        public List<BatchResult<K, V>> batch(List<BatchOperation<K, V>> batchOperations)
        {
            return batchOperations.stream().map(o -> {
                if (o instanceof GetBatchOperation) {
                    return new GetBatchResult<>(o.getKey(), target.get(o.getKey()));
                }
                else if (o instanceof ContainsKeyBatchOperation) {
                    return new ContainsKeyBatchResult<>(o.getKey(), target.containsKey(o.getKey()));
                }
                else if (o instanceof PutBatchOperation) {
                    target.put(o.getKey(), ((PutBatchOperation<K, V>) o).getValue());
                    return new PutBatchResult<>(o.getKey());
                }
                else if (o instanceof RemoveBatchOperation) {
                    target.remove(o.getKey());
                    return new RemoveBatchResult<>(o.getKey());
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }).map(r -> (BatchResult<K, V>) r).collect(toImmutableList());
        }
    }

    class KeyCodec<KO, KI, V>
            extends Kv.KeyCodec<KO, KI, V>
            implements BatchedKv<KO, V>
    {
        public KeyCodec(BatchedKv<KI, V> wrapped, Codec<KO, KI> codec)
        {
            super(wrapped, codec);
        }

        private BatchedKv<KI, V> getWrapped()
        {
            return (BatchedKv<KI, V>) wrapped;
        }

        @Override
        public List<BatchResult<KO, V>> batch(List<BatchOperation<KO, V>> batchOperations)
        {
            List<BatchOperation<KI, V>> batchOperationsI = batchOperations.stream().map(o -> {
                if (o instanceof GetBatchOperation) {
                    return new GetBatchOperation<>(codec.encode(o.getKey()));
                }
                else if (o instanceof ContainsKeyBatchOperation) {
                    return new ContainsKeyBatchOperation<>(codec.encode(o.getKey()));
                }
                else if (o instanceof PutBatchOperation) {
                    return new PutBatchOperation<>(codec.encode(o.getKey()), o.getValue());
                }
                else if (o instanceof RemoveBatchOperation) {
                    return new RemoveBatchOperation<>(codec.encode(o.getKey()));
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }).map(o -> (BatchOperation<KI, V>) o).collect(toImmutableList());
            List<BatchResult<KI, V>> batchResultsI = getWrapped().batch(batchOperationsI);
            return batchResultsI.stream().map(r -> {
                if (r instanceof GetBatchResult) {
                    return new GetBatchResult<>(codec.decode(r.getKey()), r.getValue());
                }
                else if (r instanceof ContainsKeyBatchResult) {
                    return new ContainsKeyBatchResult<>(codec.decode(r.getKey()), r.isPresent());
                }
                else if (r instanceof PutBatchResult) {
                    return new PutBatchResult<>(codec.decode(r.getKey()));
                }
                else if (r instanceof RemoveBatchResult) {
                    return new RemoveBatchResult<>(codec.decode(r.getKey()));
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }).map(r -> (BatchResult<KO, V>) r).collect(toImmutableList());
        }

        @Override
        public Map<KO, V> getAll(List<? extends KO> keys)
        {
            return getWrapped().getAll(keys.stream().map(codec::encode).collect(toImmutableList())).entrySet()
                    .stream().map(e -> Pair.immutable(codec.decode(e.getKey()), e.getValue()))
                    .collect(toImmutableMap());
        }

        @Override
        public void putAll(Map<? extends KO, ? extends V> map)
        {
            getWrapped().putAll(map.entrySet().stream()
                    .map(e -> Pair.immutable(codec.encode(e.getKey()), e.getValue()))
                    .collect(toImmutableMap()));
        }

        @Override
        public void removeAll(Set<? extends KO> keys)
        {
            getWrapped().removeAll(keys.stream().map(codec::encode).collect(toImmutableSet()));
        }

        @Override
        public Set<KO> containsKeys(Set<? extends KO> keys)
        {
            return getWrapped().containsKeys(keys.stream().map(codec::encode).collect(toImmutableSet())).stream()
                    .map(codec::decode)
                    .collect(toImmutableSet());
        }
    }

    class ValueCodec<K, VO, VI>
            extends Kv.ValueCodec<K, VO, VI>
            implements BatchedKv<K, VO>
    {
        public ValueCodec(BatchedKv<K, VI> wrapped, Codec<VO, VI> codec)
        {
            super(wrapped, codec);
        }

        private BatchedKv<K, VI> getWrapped()
        {
            return (BatchedKv<K, VI>) wrapped;
        }

        @Override
        public List<BatchResult<K, VO>> batch(List<BatchOperation<K, VO>> batchOperations)
        {
            List<BatchOperation<K, VI>> batchOperationsI = batchOperations.stream().map(o -> {
                if (o instanceof GetBatchOperation) {
                    return new GetBatchOperation<>(o.getKey());
                }
                else if (o instanceof ContainsKeyBatchOperation) {
                    return new ContainsKeyBatchOperation<>(o.getKey());
                }
                else if (o instanceof PutBatchOperation) {
                    return new PutBatchOperation<>(o.getKey(), codec.encode(o.getValue()));
                }
                else if (o instanceof RemoveBatchOperation) {
                    return new RemoveBatchOperation<>(o.getKey());
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }).map(o -> (BatchOperation<K, VI>) o).collect(toImmutableList());
            List<BatchResult<K, VI>> batchResultsI = getWrapped().batch(batchOperationsI);
            return batchResultsI.stream().map(r -> {
                if (r instanceof GetBatchResult) {
                    return new GetBatchResult<>(r.getKey(), codec.decode(r.getValue()));
                }
                else if (r instanceof ContainsKeyBatchResult) {
                    return new ContainsKeyBatchResult<>(r.getKey(), r.isPresent());
                }
                else if (r instanceof PutBatchResult) {
                    return new PutBatchResult<>(r.getKey());
                }
                else if (r instanceof RemoveBatchResult) {
                    return new RemoveBatchResult<>(r.getKey());
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }).map(r -> (BatchResult<K, VO>) r).collect(toImmutableList());
        }

        @Override
        public Map<K, VO> getAll(List<? extends K> keys)
        {
            return getWrapped().getAll(keys).entrySet().stream()
                    .map(e -> Pair.immutable(e.getKey(), codec.decode(e.getValue())))
                    .collect(toImmutableMap());
        }

        @Override
        public void putAll(Map<? extends K, ? extends VO> map)
        {
            getWrapped().putAll(map.entrySet().stream()
                    .map(e -> Pair.immutable(e.getKey(), codec.encode(e.getValue())))
                    .collect(toImmutableMap()));
        }

        @Override
        public void removeAll(Set<? extends K> keys)
        {
            getWrapped().removeAll(keys);
        }

        @Override
        public Set<K> containsKeys(Set<? extends K> keys)
        {
            return getWrapped().containsKeys(keys);
        }
    }
}
