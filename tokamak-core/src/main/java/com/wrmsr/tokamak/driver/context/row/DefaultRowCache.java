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
package com.wrmsr.tokamak.driver.context.row;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.diag.Stat;
import com.wrmsr.tokamak.node.Node;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public class DefaultRowCache
        implements RowCache
{
    /*
    TODO:
     - StatefulNodal
     - share FieldKey structure
     - custom datastructure prob
     - kfv map values are bools? just usedForKey?
      - diff strats: when all fk's kv's have been usedForKey return (and thus store maps per kv) or just block puts?
       - policies
     - limited storage / eviction ala mysql joincache

    Map<String, Map<Object, Boolean>> isKeyByValueByKeyField = new HashMap<>(); // TODO: HPPC
    Map<String, Map<Object, KeyFieldValueEntry>> keyFieldValueEntriesByValueByKeyField = new HashMap<>();
    */

    private static final class KeyFieldValueEntry
    {
        final String field;

        @Nullable final Object value;

        boolean usedForKey;
        final Set<DriverRow> rows = new HashSet<>();

        KeyFieldValueEntry(String field, @Nullable Object value)
        {
            this.field = checkNotNull(field);
            this.value = value;
        }
    }

    private final class Nodal
    {
        final Node node;

        final Set<DriverRow> allRows = new HashSet<>();
        final Map<ImmutableSet<String>, Map<ImmutableList<Object>, Set<DriverRow>>> rowSetsByKeyValueListByKeyFieldSet = new HashMap<>();
        final Map<String, Map<Object, KeyFieldValueEntry>> keyFieldValueEntriesByValueByKeyField = new HashMap<>();
        final Map<Id, Set<DriverRow>> rowSetsById = new HashMap<>();
        boolean allRequested;

        final Map<ImmutableSet<String>, ImmutableSet<String>> orderedFieldSets = new HashMap<>();

        Nodal(Node node)
        {
            this.node = checkNotNull(node);
        }

        ImmutableSet<String> orderKeyFields(Set<String> fields)
        {
            return orderedFieldSets.computeIfAbsent(ImmutableSet.copyOf(fields), fs ->
                    fs.asList().stream()
                            .sorted(Comparator.comparing(node.getRowLayout().getPositionsByField()::get))
                            .collect(toImmutableSet()));
        }

        ImmutableList<Object> orderKeyValues(ImmutableSet<String> orderedFields, Map<String, Object> valuesByField)
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
                return Optional.ofNullable(rowSetsById.get(((IdKey) key).getId()));
            }

            else if (key instanceof FieldKey) {
                FieldKey fieldKey = (FieldKey) key;

                ImmutableSet<String> orderedKeyFields = orderKeyFields(fieldKey.getFields());
                Map<ImmutableList<Object>, Set<DriverRow>> rowSetsByKeyValueList = rowSetsByKeyValueListByKeyFieldSet.get(orderedKeyFields);
                if (rowSetsByKeyValueList == null) {
                    return Optional.empty();
                }

                ImmutableList<Object> orderedKeyValues = orderKeyValues(orderedKeyFields, fieldKey.getValuesByField());
                Set<DriverRow> rows = rowSetsByKeyValueList.get(orderedKeyValues);
                return Optional.ofNullable(rows);
            }

            else {
                throw new IllegalArgumentException(Objects.toString(key));
            }
        }

        void backfillKeyField(String field)
        {
            checkState(!keyFieldValueEntriesByValueByKeyField.containsKey(field));
            Map<Object, KeyFieldValueEntry> map = new HashMap<>();
            keyFieldValueEntriesByValueByKeyField.put(field, map);
            int pos = node.getRowLayout().getPositionsByField().get(field);
            for (DriverRow row : allRows) {
                if (row.isNull()) {
                    continue;
                }
                Object value = row.getAttributes()[pos];
                KeyFieldValueEntry entry = map.computeIfAbsent(value, v -> new KeyFieldValueEntry(field, value));
                entry.rows.add(row);
            }
        }

        boolean maybeBackfillKeyField(String field)
        {
            if (!keyFieldValueEntriesByValueByKeyField.containsKey(field)) {
                backfillKeyField(field);
                return true;
            }
            else {
                return false;
            }
        }

        void put(Key key, Collection<DriverRow> rows)
        {
            checkArgument(!rows.isEmpty());

            if (key instanceof AllKey) {
                checkState(!allRequested);
                // allRows = ImmutableSet.copyOf(rows);
                throw new IllegalArgumentException(Objects.toString(key));
            }

            else if (key instanceof IdKey) {
                throw new IllegalArgumentException(Objects.toString(key));
            }

            else if (key instanceof FieldKey) {
                FieldKey fieldKey = (FieldKey) key;

                checkState(!allRequested);

                // Check all rows have correct key fields
                for (Map.Entry<String, Object> keyEntry : fieldKey.getValuesByField().entrySet()) {
                    int pos = node.getRowLayout().getPositionsByField().get(keyEntry.getKey());
                    Object value = keyEntry.getValue();
                    for (DriverRow row : rows) {
                        if (row.isNull()) {
                            continue;
                        }
                        checkArgument(Objects.equals(row.getAttributes()[pos], value));
                    }
                }

                // Add to FieldKey map
                ImmutableSet<String> orderedKeyFields = orderKeyFields(fieldKey.getFields());
                ImmutableList<Object> orderedKeyValues = orderKeyValues(orderedKeyFields, fieldKey.getValuesByField());
                Map<ImmutableList<Object>, Set<DriverRow>> rowSetsBykeyValueList =
                        rowSetsByKeyValueListByKeyFieldSet.computeIfAbsent(orderedKeyFields, kf -> new HashMap<>());
                if (rowSetsBykeyValueList.get(orderedKeyValues) != null) {
                    throw new KeyException(key);
                }
                rowSetsBykeyValueList.put(orderedKeyValues, ImmutableSet.copyOf(rows));

                // Backfill any FieldValue maps *before* adding to all rows
                fieldKey.getValuesByField().keySet().forEach(this::maybeBackfillKeyField);

                // Add FieldKey fields to FieldValue map marked as usedForKey
                for (Map.Entry<String, Object> keyEntry : fieldKey.getValuesByField().entrySet()) {
                    String field = keyEntry.getKey();
                    Object value = keyEntry.getValue();
                    Map<Object, KeyFieldValueEntry> map = keyFieldValueEntriesByValueByKeyField.get(field);
                    KeyFieldValueEntry entry = map.computeIfAbsent(value, v -> new KeyFieldValueEntry(field, value));
                    if (entry.usedForKey) {
                        if (!entry.rows.equals(rows)) {
                            throw new KeyException(key);
                        }
                    }
                    else {
                        if (!rows.containsAll(entry.rows)) {
                            throw new KeyException(key);
                        }
                        entry.rows.addAll(rows);
                        entry.usedForKey = true;
                    }
                }

                // Add non-FieldKey fields to FieldValue map and check not usedForKey
                for (Map.Entry<String, Map<Object, KeyFieldValueEntry>> e : keyFieldValueEntriesByValueByKeyField.entrySet()) {
                    String field = e.getKey();
                    if (fieldKey.getFields().contains(field)) {
                        continue;
                    }
                    int pos = node.getRowLayout().getPositionsByField().get(field);
                    Map<Object, KeyFieldValueEntry> map = e.getValue();
                    for (DriverRow row : rows) {
                        Object value = row.getAttributes()[pos];
                        KeyFieldValueEntry entry = map.computeIfAbsent(value, v -> new KeyFieldValueEntry(field, value));
                        if (entry.usedForKey) {
                            if (!entry.rows.contains(row)) {
                                throw new KeyException(key);
                            }
                        }
                        else {
                            entry.rows.add(row);
                        }
                    }
                }

                // FIXME:
                // Add to Id map if necessary
                // for (DriverRow row : rows) {
                //     if (row.getId() == null) {
                //         checkState(row.isNull());
                //         continue;
                //     }
                //     checkState(!rowSetsById.containsKey(row.getId()));
                //     rowSetsById.put(row.getId(), ImmutableSet.of(row);
                // }

                // Finally add to all rows
                allRows.addAll(rows);
            }

            else {
                throw new IllegalArgumentException(Objects.toString(key));
            }
        }
    }

    private final Stat.Updater statUpdater;

    private Map<Node, Nodal> byNode = new HashMap<>();

    public DefaultRowCache(Stat.Updater statUpdater)
    {
        this.statUpdater = checkNotNull(statUpdater);
    }

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
