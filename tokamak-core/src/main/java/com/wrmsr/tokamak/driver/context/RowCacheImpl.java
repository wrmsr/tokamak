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
package com.wrmsr.tokamak.driver.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public class RowCacheImpl
        implements RowCache
{
    /*
    TODO:
     - StatefulNodal
     - share FieldKey structure
    */

    private class Nodal
    {
        final Node node;

        final Set<DriverRow> allRows = new HashSet<>();
        final Map<Id, Set<DriverRow>> rowsById = new HashMap<>();
        final Map<ImmutableSet<String>, Map<ImmutableList<Object>, Set<DriverRow>>> rowsByValuesByFields = new HashMap<>();
        boolean allRequested;

        final Map<ImmutableSet<String>, ImmutableSet<String>> orderedFieldSets = new HashMap<>();

        Nodal(Node node)
        {
            this.node = checkNotNull(node);
        }

        ImmutableSet<String> orderFieldSet(ImmutableSet<String> fields)
        {
            return orderedFieldSets.computeIfAbsent(ImmutableSet.copyOf(fields), fs ->
                    fs.asList().stream()
                            .sorted(Comparator.comparing(node.getRowLayout().getPositionsByField()::get))
                            .collect(toImmutableSet()));
        }

        ImmutableList<Object> orderValues(Collection<String> orderedFields, Map<String, Object> valuesByField)
        {
            ImmutableList.Builder<Object> builder = ImmutableList.builderWithExpectedSize(orderedFields.size());
            for (String field : orderedFields) {
                builder.add(valuesByField.get(field));
            }
            return builder.build();
        }

        Optional<Collection<DriverRow>> get(Key key)
        {
            if (key instanceof AllKey) {
                return allRequested ? Optional.of(Collections.unmodifiableSet(allRows)) : Optional.empty();
            }

            else if (key instanceof IdKey) {
                return Optional.ofNullable(rowsById.get(((IdKey) key).getId()));
            }

            else if (key instanceof FieldKey) {
                FieldKey fieldKey = (FieldKey) key;

                ImmutableSet<String> keyFields = ImmutableSet.copyOf(fieldKey.getValuesByField().keySet());
                Map<ImmutableList<Object>, Set<DriverRow>> rowsByValues = rowsByValuesByFields.get(keyFields);
                if (rowsByValues == null) {
                    return Optional.empty();
                }

                ImmutableSet<String> orderedKeyFields = orderFieldSet(keyFields);
                ImmutableList<Object> orderedValues = orderValues(orderedKeyFields, fieldKey.getValuesByField());
                Set<DriverRow> rows = rowsByValues.get(orderedValues);
                return Optional.ofNullable(rows);
            }

            else {
                throw new IllegalArgumentException(key.toString());
            }
        }

        void put(Key key, Collection<DriverRow> rows)
        {
            checkArgument(!rows.isEmpty());

            if (key instanceof AllKey) {
                checkState(!allRequested);
                // allRows = ImmutableSet.copyOf(rows);
                throw new IllegalArgumentException(key.toString());
            }

            else if (key instanceof IdKey) {
                throw new IllegalArgumentException(key.toString());
            }

            else if (key instanceof FieldKey) {
                throw new IllegalArgumentException(key.toString());
            }

            else {
                throw new IllegalArgumentException(key.toString());
            }
        }
    }

    private Map<Node, Nodal> byNode = new HashMap<>();

    @Override
    public Optional<Collection<DriverRow>> get(Node node, Key key)
    {
        Nodal nodal = byNode.get(node);
        if (nodal == null) {
            return Optional.empty();
        }
        return nodal.get(key);
    }

    @Override
    public void put(Node node, Key key, Collection<DriverRow> rows)
    {
        byNode.computeIfAbsent(node, n -> new Nodal(node)).put(key, rows);
    }

    @Override
    public Collection<DriverRow> getForNode(Node node)
    {
        Nodal nodal = byNode.get(node);
        if (nodal == null) {
            return ImmutableSet.of();
        }
        return Collections.unmodifiableSet(nodal.allRows);
    }

    @Override
    public Map<Node, Collection<DriverRow>> getNodeMap()
    {
        return byNode.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> Collections.unmodifiableSet(e.getValue().allRows)));
    }
}
