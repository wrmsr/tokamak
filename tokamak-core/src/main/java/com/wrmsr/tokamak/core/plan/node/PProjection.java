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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static java.util.function.Function.identity;

@Immutable
public final class PProjection
        implements StreamableIterable<Map.Entry<String, PProjection.Input>>
{
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = FieldInput.class, name = "field"),
            @JsonSubTypes.Type(value = FunctionInput.class, name = "function"),
    })
    public interface Input
    {
        static FieldInput of(String field)
        {
            return new FieldInput(field);
        }

        static FunctionInput of(PFunction function, List<String> args)
        {
            return new FunctionInput(function, args);
        }
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
            this.field = checkNotNull(field);
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
        private final PFunction function;
        private final List<String> args;

        @JsonCreator
        public FunctionInput(
                @JsonProperty("function") PFunction function,
                @JsonProperty("args") List<String> args)
        {
            this.function = checkNotNull(function);
            this.args = ImmutableList.copyOf(args);
            checkArgument(function.getType().getParamTypes().size() == this.args.size());
        }

        @Override
        public String toString()
        {
            return "FunctionInput{" +
                    "function=" + function +
                    ", args=" + args +
                    '}';
        }

        @JsonProperty("function")
        public PFunction getFunction()
        {
            return function;
        }

        @JsonProperty("args")
        public List<String> getArgs()
        {
            return args;
        }
    }

    private final OrderPreservingImmutableMap<String, Input> inputsByOutput;

    private final Map<String, String> inputFieldsByOutput;
    private final Map<String, Set<String>> outputSetsByInputField;

    @JsonCreator
    public PProjection(
            @JsonProperty("inputsByOutput") OrderPreservingImmutableMap<String, Input> inputsByOutput)
    {
        this.inputsByOutput = checkNotNull(inputsByOutput);

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

    public PProjection(Map<String, Input> inputsByOutput)
    {
        this(new OrderPreservingImmutableMap<>(checkOrdered(inputsByOutput)));
    }

    @Override
    public Iterator<Map.Entry<String, Input>> iterator()
    {
        return inputsByOutput.entrySet().iterator();
    }

    @JsonProperty("inputsByOutput")
    public OrderPreservingImmutableMap<String, Input> getInputsByOutput()
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

    public static PProjection only(Iterable<String> fields)
    {
        return new PProjection(
                StreamSupport.stream(fields.spliterator(), false)
                        .collect(toImmutableMap(identity(), FieldInput::new)));
    }

    public static PProjection of(Object... args)
    {
        ImmutableMap.Builder<String, Input> builder = ImmutableMap.builder();
        for (int i = 0; i < args.length; ) {
            String output = (String) args[i++];
            Object inputObj = checkNotNull(args[i++]);
            Input input;
            if (inputObj instanceof String) {
                input = new FieldInput((String) inputObj);
            }
            else if (inputObj instanceof Executable || inputObj instanceof PFunction || inputObj instanceof com.wrmsr.tokamak.core.catalog.Function) {
                PFunction function = inputObj instanceof Executable ?
                        PFunction.of((Executable) inputObj) :
                        inputObj instanceof com.wrmsr.tokamak.core.catalog.Function ? ((com.wrmsr.tokamak.core.catalog.Function) inputObj).asNodeFunction() :
                                (PFunction) inputObj;
                ImmutableList.Builder<String> funcArgs = ImmutableList.builder();
                for (Type param : function.getType().getParamTypes()) {
                    funcArgs.add((String) args[i++]);
                }
                input = new FunctionInput(function, funcArgs.build());
            }
            else {
                throw new IllegalArgumentException(Objects.toString(inputObj));
            }
            builder.put(output, input);
        }
        return new PProjection(builder.build());
    }

    public static PProjection of(Map<String, String> map)
    {
        return new PProjection(map.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> PProjection.Input.of(e.getValue()))));
    }
}
