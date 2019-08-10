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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.function.Function;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.sun.tools.javac.util.Assert.checkNonNull;
import static java.util.function.Function.identity;

@Immutable
public final class Projection
{
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = FieldInput.class, name = "field"),
            @JsonSubTypes.Type(value = FunctionInput.class, name = "function")
    })
    public interface Input
    {
    }

    @Immutable
    public static final class FieldInput
            implements Input
    {
        private final String field;

        @JsonCreator
        public FieldInput(
                @JsonProperty("field") String field)
        {
            this.field = checkNonNull(field);
        }

        @Override
        public String toString()
        {
            return "FieldInput{" +
                    "field='" + field + '\'' +
                    '}';
        }

        @JsonProperty("field")
        public String getField()
        {
            return field;
        }
    }

    @Immutable
    public static final class FunctionInput
            implements Input
    {
        private final Function function;
        private final Type type;

        @JsonCreator
        public FunctionInput(
                @JsonProperty("function") Function function,
                @JsonProperty("type") Type type)
        {
            this.function = checkNotNull(function);
            this.type = checkNotNull(type);
        }

        @Override
        public String toString()
        {
            return "FunctionInput{" +
                    "function=" + function +
                    ", type=" + type +
                    '}';
        }

        @JsonProperty("function")
        public Function getFunction()
        {
            return function;
        }

        @JsonProperty("type")
        public Type getType()
        {
            return type;
        }
    }

    private final Map<String, Input> inputsByOutput;

    private final Map<String, String> inputFieldsByOutput;
    private final Map<String, Set<String>> outputSetsByInputField;

    @JsonCreator
    public Projection(
            @JsonProperty("inputsByOutput") Map<String, Input> inputsByOutput)
    {
        this.inputsByOutput = ImmutableMap.copyOf(inputsByOutput);

        ImmutableMap.Builder<String, String> inputFieldsByOutput = ImmutableMap.builder();
        Map<String, ImmutableSet.Builder<String>> outputSetsByInputField = new HashMap<>();

        for (Map.Entry<String, Input> entry : this.inputsByOutput.entrySet()) {
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

    @JsonSerialize(using = OrderPreservingImmutableMap.Serializer.class)
    @JsonDeserialize(using = OrderPreservingImmutableMap.Deserializer.class)
    @JsonProperty("inputsByOutput")
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

    public static Projection of(Object... args)
    {
        checkArgument(args.length % 2 == 0);
        ImmutableMap.Builder<String, Input> builder = ImmutableMap.builder();
        for (int i = 0; i < args.length; ) {
            String output = (String) args[i++];
            Object inputObj = checkNotNull(args[i++]);
            Input input;
            if (inputObj instanceof String) {
                input = new FieldInput((String) inputObj);
            }
            else if (inputObj instanceof Function) {
                input = new FunctionInput((Function) inputObj, (Type) args[i++]);
            }
            else {
                throw new IllegalArgumentException(inputObj.toString());
            }
            builder.put(output, input);
        }
        return new Projection(builder.build());
    }
}
