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
package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.function.Function;
import com.wrmsr.tokamak.util.Box;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public final class Projection
{
    public interface Input
    {
    }

    @Immutable
    public static final class FieldInput
            extends Box<FieldName>
            implements Input
    {
        public FieldInput(FieldName value)
        {
            super(value);
        }
    }

    @Immutable
    public static final class FunctionInput
            extends Box<Function>
            implements Input
    {
        public FunctionInput(Function value)
        {
            super(value);
        }
    }

    private final Map<FieldName, Input> inputsByOutput;

    private final Map<FieldName, FieldName> inputFieldsByOutput;
    private final Map<FieldName, Set<FieldName>> outputSetsByInputField;

    public Projection(Map<FieldName, Input> inputsByOutput)
    {
        this.inputsByOutput = ImmutableMap.copyOf(inputsByOutput);

        ImmutableMap.Builder<FieldName, FieldName> inputFieldsByOutput = ImmutableMap.builder();
        Map<FieldName, ImmutableSet.Builder<FieldName>> outputSetsByInputField = new HashMap<>();

        for (Map.Entry<FieldName, Input> entry : inputsByOutput.entrySet()) {
            if (entry.getValue() instanceof FieldInput) {
                FieldName field = ((FieldInput) entry.getValue()).getValue();
                inputFieldsByOutput.put(entry.getKey(), field);
                outputSetsByInputField.computeIfAbsent(field, v -> ImmutableSet.builder()).add(entry.getKey());
            }
        }

        this.inputFieldsByOutput = inputFieldsByOutput.build();
        this.outputSetsByInputField = outputSetsByInputField.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
    }

    public Map<FieldName, Input> getInputsByOutput()
    {
        return inputsByOutput;
    }

    public Map<FieldName, FieldName> getInputFieldsByOutput()
    {
        return inputFieldsByOutput;
    }

    public Map<FieldName, Set<FieldName>> getOutputSetsByInputField()
    {
        return outputSetsByInputField;
    }

    public static Projection only(Iterable<FieldName> fields)
    {
        return new Projection(
                StreamSupport.stream(fields.spliterator(), false)
                        .collect(toImmutableMap(identity(), FieldInput::new)));
    }
}
