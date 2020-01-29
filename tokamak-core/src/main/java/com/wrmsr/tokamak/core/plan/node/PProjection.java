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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.plan.value.VNodes;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static java.util.function.Function.identity;

@Immutable
public final class PProjection
        implements StreamableIterable<Map.Entry<String, VNode>>
{
    private final OrderPreservingImmutableMap<String, VNode> inputsByOutput;

    private final Map<String, String> inputFieldsByOutput;
    private final Map<String, Set<String>> outputSetsByInputField;
    private final Set<String> forwardedFields;
    private final boolean isForward;

    @JsonCreator
    public PProjection(
            @JsonProperty("inputsByOutput") OrderPreservingImmutableMap<String, VNode> inputsByOutput)
    {
        this.inputsByOutput = checkNotNull(inputsByOutput);

        ImmutableMap.Builder<String, String> inputFieldsByOutput = ImmutableMap.builder();
        Map<String, ImmutableSet.Builder<String>> outputSetsByInputField = new HashMap<>();
        ImmutableSet.Builder<String> forwardedFields = ImmutableSet.builder();

        for (Map.Entry<String, VNode> entry : this.inputsByOutput.entrySet()) {
            if (entry.getValue() instanceof VField) {
                String field = ((VField) entry.getValue()).getField();
                inputFieldsByOutput.put(entry.getKey(), field);
                outputSetsByInputField.computeIfAbsent(field, v -> ImmutableSet.builder()).add(entry.getKey());
                if (entry.getKey().equals(field)) {
                    forwardedFields.add(field);
                }
            }
        }

        this.inputFieldsByOutput = inputFieldsByOutput.build();
        this.outputSetsByInputField = outputSetsByInputField.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
        this.forwardedFields = forwardedFields.build();

        isForward = this.forwardedFields.equals(inputsByOutput.keySet());
    }

    public PProjection(Map<String, VNode> inputsByOutput)
    {
        this(new OrderPreservingImmutableMap<>(checkOrdered(inputsByOutput)));
    }

    @Override
    public Iterator<Map.Entry<String, VNode>> iterator()
    {
        return inputsByOutput.entrySet().iterator();
    }

    @JsonProperty("inputsByOutput")
    public OrderPreservingImmutableMap<String, VNode> getInputsByOutput()
    {
        return inputsByOutput;
    }

    public Map<String, String> getInputFieldsByOutput()
    {
        return inputFieldsByOutput;
    }

    public Map<String, Set<String>> getOutputSetsByInputField()
    {
        return outputSetsByInputField;
    }

    public Set<String> getForwardedFields()
    {
        return forwardedFields;
    }

    public boolean isForward()
    {
        return isForward;
    }

    public static PProjection only(Iterable<String> fields)
    {
        return new PProjection(
                StreamSupport.stream(fields.spliterator(), false)
                        .collect(toImmutableMap(identity(), VNodes::field)));
    }

    public static PProjection only(String... fields)
    {
        return only(ImmutableList.copyOf(fields));
    }
}
