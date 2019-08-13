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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.function.BinaryFunction;
import com.wrmsr.tokamak.function.Function;
import com.wrmsr.tokamak.function.NullaryFunction;
import com.wrmsr.tokamak.function.RowFunction;
import com.wrmsr.tokamak.function.RowViewFunction;
import com.wrmsr.tokamak.function.UnaryFunction;
import com.wrmsr.tokamak.function.VariadicFunction;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.OrderPreservingImmutableMap;
import com.wrmsr.tokamak.util.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.sun.tools.javac.util.Assert.checkNonNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
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
                    Map<String, Object> map = parser.readValueAs(new TypeReference<Map<String, Object>>() {});
                    checkState(map.keySet().equals(ImmutableSet.of("function", "type", "args")));
                    return new FunctionInput(
                            (String) map.get("function"),
                            Type.parseRepr((String) map.get("type")),
                            (List<String>) map.get("args"));
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
        private final List<String> args;

        public FunctionInput(String function, Type type, List<String> args)
        {
            this.function = checkNotNull(function);
            this.type = checkNotNull(type);
            this.args = ImmutableList.copyOf(args);
        }

        @Override
        public String toString()
        {
            return "FunctionInput{" +
                    "function='" + function + '\'' +
                    ", type=" + type +
                    ", args=" + args +
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

        @JsonProperty("args")
        public List<String> getArgs()
        {
            return args;
        }
    }

    private final Map<String, Input> inputsByOutput;

    private final Map<String, String> inputFieldsByOutput;
    private final Map<String, Set<String>> outputSetsByInputField;

    public Projection(Map<String, Input> inputsByOutput)
    {
        this.inputsByOutput = new OrderPreservingImmutableMap<>(ImmutableMap.copyOf(checkOrdered(inputsByOutput)));

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
                List<String> funcArgs;
                if (func instanceof RowFunction || func instanceof RowViewFunction || func instanceof NullaryFunction) {
                    funcArgs = ImmutableList.of();
                }
                else if (func instanceof UnaryFunction) {
                    funcArgs = ImmutableList.of((String) args[i++]);
                }
                else if (func instanceof BinaryFunction) {
                    funcArgs = ImmutableList.of((String) args[i++], (String) args[i++]);
                }
                else if (func instanceof VariadicFunction) {
                    funcArgs = ImmutableList.copyOf((String[]) args[i++]);
                }
                else {
                    throw new IllegalArgumentException(func.toString());
                }
                input = new FunctionInput(func.getName(), func.getType(), funcArgs);
            }
            else {
                throw new IllegalArgumentException(inputObj.toString());
            }
            builder.put(output, input);
        }
        return new Projection(builder.build());
    }
}
