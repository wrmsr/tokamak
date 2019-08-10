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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.function.Function;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.OrderPreservingImmutableMap;
import com.wrmsr.tokamak.util.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.sun.tools.javac.util.Assert.checkNonNull;
import static java.util.function.Function.identity;

@Immutable
public final class Projection
        implements StreamableIterable<Map.Entry<String, Projection.Input>>
{
    @JsonDeserialize(using = Projection.Input.Deserializer.class)
    public interface Input
    {
        final class Deserializer
                extends JsonDeserializer<Input>
        {
            @Override
            public Input deserialize(JsonParser parser, DeserializationContext ctx)
                    throws IOException
            {
                if (parser.currentToken() == JsonToken.VALUE_STRING) {
                    return new FieldInput(parser.getValueAsString());
                }
                else if (parser.currentToken() == JsonToken.START_OBJECT) {
                    Map<String, String> map = parser.readValueAs(new TypeReference<Map<String, String>>() {});
                    checkState(map.keySet().equals(ImmutableSet.of("function", "type")));
                    return new FunctionInput(map.get("function"), Type.parseRepr(map.get("type")));
                }
                else {
                    throw new IllegalStateException();
                }
            }
        }
    }

    @Immutable
    public static final class FieldInput
            implements Input
    {
        private final String field;

        public FieldInput(String field)
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

        @JsonValue
        public String getField()
        {
            return field;
        }
    }

    @Immutable
    public static final class FunctionInput
            implements Input
    {
        private final String function;
        private final Type type;

        public FunctionInput(String function, Type type)
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
        public String getFunction()
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

    public Projection(Map<String, Input> inputsByOutput)
    {
        this.inputsByOutput = new OrderPreservingImmutableMap<>(ImmutableMap.copyOf(inputsByOutput));

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

    @Override
    public Iterator<Map.Entry<String, Input>> iterator()
    {
        return inputsByOutput.entrySet().iterator();
    }

    @JsonValue
    public Map<String, Input> getInputsByOutput()
    {
        return inputsByOutput;
    }

    @JsonCreator
    public static Projection fromOrderPreservingImmutableMap(OrderPreservingImmutableMap<String, Input> map)
    {
        return new Projection(map);
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
                Function func = (Function) inputObj;
                input = new FunctionInput(func.getName(), func.getType());
            }
            else {
                throw new IllegalArgumentException(inputObj.toString());
            }
            builder.put(output, input);
        }
        return new Projection(builder.build());
    }
}
