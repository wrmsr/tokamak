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
package com.wrmsr.tokamak.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.function.Function;
import com.wrmsr.tokamak.type.Type;

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
            implements Input
    {
        private final String field;

        public FieldInput(String field)
        {
            this.field = field;
        }

        public String getField()
        {
            return field;
        }
    }

    @Immutable
    public static final class FunctionInput
            implements Input
    {
        private final Function value;
        private final Type type;

        public FunctionInput(Function value, Type type)
        {
            this.value = value;
            this.type = type;
        }

        public Function getValue()
        {
            return value;
        }

        public Type getType()
        {
            return type;
        }
    }

    private final Map<String, Input> inputsByOutput;

    private final Map<String, String> inputFieldsByOutput;
    private final Map<String, Set<String>> outputSetsByInputField;

    public Projection(Map<String, Input> inputsByOutput)
    {
        this.inputsByOutput = ImmutableMap.copyOf(inputsByOutput);

        ImmutableMap.Builder<String, String> inputFieldsByOutput = ImmutableMap.builder();
        Map<String, ImmutableSet.Builder<String>> outputSetsByInputField = new HashMap<>();

        for (Map.Entry<String, Input> entry : inputsByOutput.entrySet()) {
            if (entry.getValue() instanceof FieldInput) {
                String field = ((FieldInput) entry.getValue()).getField();
                inputFieldsByOutput.put(entry.getKey(), field);
                outputSetsByInputField.computeIfAbsent(field, v -> ImmutableSet.builder()).add(entry.getKey());
            }
        }

        this.inputFieldsByOutput = inputFieldsByOutput.build();
        this.outputSetsByInputField = outputSetsByInputField.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
    }

    public Map<String, Input> getInputsByOutput()
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

    public static Projection only(Iterable<String> fields)
    {
        return new Projection(
                StreamSupport.stream(fields.spliterator(), false)
                        .collect(toImmutableMap(identity(), FieldInput::new)));
    }
}
