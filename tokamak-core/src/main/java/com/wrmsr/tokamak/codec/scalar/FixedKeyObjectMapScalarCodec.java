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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.codec.Width;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreOptionals.reduceOptionals;

@Immutable
@SuppressWarnings({"rawtypes"})
public final class FixedKeyObjectMapScalarCodec<K>
        implements ScalarCodec<Map<K, Object>>
{
    private final Map<K, ScalarCodec> childrenByKey;
    private final List<Pair<K, ScalarCodec>> keyChildPairs;
    private final ObjectArrayBackedMap.Shape<K> shape;
    private final boolean strict;

    public FixedKeyObjectMapScalarCodec(Map<K, ScalarCodec> childrenByKey, boolean strict)
    {
        this.childrenByKey = ImmutableMap.copyOf(checkOrdered(childrenByKey));
        this.keyChildPairs = this.childrenByKey.entrySet().stream().map(Pair::immutable).collect(toImmutableList());
        this.shape = ObjectArrayBackedMap.Shape.of(this.childrenByKey.keySet());
        this.strict = strict;
    }

    private final SupplierLazyValue<Width> width = new SupplierLazyValue<>();

    @Override
    public Width getWidth()
    {
        return width.get(() -> {
            List<Width> childWidths = childrenByKey.values().stream().map(ScalarCodec::getWidth).collect(toImmutableList());
            return Width.of(
                    childWidths.stream().map(Width::getMin).reduce(0, Integer::sum),
                    reduceOptionals(0, Integer::sum, childWidths.stream().map(Width::getMax).iterator()));
        });
    }

    public Map<K, ScalarCodec> getChildrenByKey()
    {
        return childrenByKey;
    }

    public ObjectArrayBackedMap.Shape<K> getShape()
    {
        return shape;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void encode(Map<K, Object> value, Output output)
    {
        if (strict) {
            checkArgument(value.keySet().equals(childrenByKey.keySet()));
        }
        for (Pair<K, ScalarCodec> e : keyChildPairs) {
            e.getValue().encode(value.get(e.getKey()), output);
        }
    }

    @Override
    public Map<K, Object> decode(Input input)
    {
        Object[] values = new Object[childrenByKey.size()];
        for (int i = 0; i < childrenByKey.size(); ++i) {
            Pair<K, ScalarCodec> e = keyChildPairs.get(i);
            values[i] = e.getValue().decode(input);
        }
        return new ObjectArrayBackedMap<>(shape, values);
    }
}
