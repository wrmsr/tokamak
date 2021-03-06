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
package com.wrmsr.tokamak.core.serde.impl;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Width;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreOptionals.reduceOptionals;

@Immutable
@SuppressWarnings({"rawtypes"})
public final class FixedKeyObjectMapSerde<K>
        implements Serde<Map<K, Object>>
{
    private final Map<K, Serde> childrenByKey;
    private final List<Pair<K, Serde>> keyChildPairs;
    private final ObjectArrayBackedMap.Shape<K> shape;
    private final boolean strict;

    public FixedKeyObjectMapSerde(Map<K, Serde> childrenByKey, boolean strict)
    {
        this.childrenByKey = ImmutableMap.copyOf(checkOrdered(childrenByKey));
        this.keyChildPairs = immutableMapItems(this.childrenByKey.entrySet(), Pair::immutable);
        this.shape = ObjectArrayBackedMap.Shape.of(this.childrenByKey.keySet());
        this.strict = strict;
    }

    private final SupplierLazyValue<Width> width = new SupplierLazyValue<>();

    @Override
    public Width getWidth()
    {
        return width.get(() -> {
            List<Width> childWidths = immutableMapItems(childrenByKey.values(), Serde::getWidth);
            return Width.of(
                    childWidths.stream().map(Width::getMin).reduce(0, Integer::sum),
                    reduceOptionals(0, Integer::sum, childWidths.stream().map(Width::getMax).iterator()));
        });
    }

    public Map<K, Serde> getChildrenByKey()
    {
        return childrenByKey;
    }

    public ObjectArrayBackedMap.Shape<K> getShape()
    {
        return shape;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void write(Map<K, Object> value, Output output)
    {
        if (strict) {
            checkArgument(value.keySet().equals(childrenByKey.keySet()));
        }
        for (Pair<K, Serde> e : keyChildPairs) {
            e.getValue().write(value.get(e.getKey()), output);
        }
    }

    @Override
    public Map<K, Object> read(Input input)
    {
        Object[] values = new Object[childrenByKey.size()];
        for (int i = 0; i < childrenByKey.size(); ++i) {
            Pair<K, Serde> e = keyChildPairs.get(i);
            values[i] = e.getValue().read(input);
        }
        return new ObjectArrayBackedMap<>(shape, values);
    }
}
