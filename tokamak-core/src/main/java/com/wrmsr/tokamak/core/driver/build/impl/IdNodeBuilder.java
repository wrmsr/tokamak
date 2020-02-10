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

package com.wrmsr.tokamak.core.driver.build.impl;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Serdes;
import com.wrmsr.tokamak.core.serde.impl.TupleSerde;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public interface IdNodeBuilder<T extends PNode>
        extends Builder<T>
{
    default List<String> getOrderedIdFields()
    {
        return immutableMapItems(getNode().getFields().getFieldListsByAnnotationCls().get(IdField.class), Field::getName);
    }

    default Set<String> getIdFields()
    {
        return ImmutableSet.copyOf(getOrderedIdFields());
    }

    default Serde<Object[]> getIdSerde()
    {
        return new TupleSerde(
                getOrderedIdFields().stream()
                        .map(getNode().getFields()::getType)
                        .map(Serdes.VALUE_SERDES_BY_TYPE::get)
                        .collect(toImmutableList()));
    }

    static Id newId(Key key, Set<String> idFields, List<String> orderedIdFields, Serde<Object[]> idSerde)
    {
        if (key.getFields().equals(idFields)) {
            Object[] idAtts = new Object[orderedIdFields.size()];
            for (int i = 0; i < idAtts.length; ++i) {
                idAtts[i] = key.get(orderedIdFields.get(i));
            }
            return Id.of(idSerde.writeBytes(idAtts));
        }
        else {
            return null;
        }
    }

    static Id newId(Map<String, Object> rowMap, List<String> orderedIdFields, Serde<Object[]> idSerde)
    {
        Object[] idAtts = new Object[orderedIdFields.size()];
        for (int i = 0; i < idAtts.length; ++i) {
            idAtts[i] = rowMap.get(orderedIdFields.get(i));
        }
        return Id.of(idSerde.writeBytes(idAtts));
    }
}
