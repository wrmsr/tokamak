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
package com.wrmsr.tokamak.core.search;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.search.node.SAnd;
import com.wrmsr.tokamak.core.search.node.SCompare;
import com.wrmsr.tokamak.core.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search.node.SCurrent;
import com.wrmsr.tokamak.core.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search.node.SFlattenArray;
import com.wrmsr.tokamak.core.search.node.SFlattenObject;
import com.wrmsr.tokamak.core.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search.node.SIndex;
import com.wrmsr.tokamak.core.search.node.SJsonLiteral;
import com.wrmsr.tokamak.core.search.node.SNegate;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SParameter;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.SSlice;
import com.wrmsr.tokamak.core.search.node.SString;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollections.streamIterator;

public final class Evaluation
{
    /*
    Abs
    Avg
    Contains
    Ceil
    EndsWith
    Floor
    Join
    Keys
    Length
    Map
    Max
    MaxBy
    Merge
    Min
    MinBy
    NotNull
    Reverse
    Sort
    SortBy
    StartsWith
    Sum
    ToArray
    ToString
    ToNumber
    Type
    Values
    */

    private Evaluation()
    {
    }

    public enum ValueType
    {
        NUMBER,
        STRING,
        BOOLEAN,
        ARRAY,
        OBJECT,
        NULL,
    }

    public interface Arg<T>
    {
        T getValue();
    }

    public static final class ValueArg<T>
            implements Arg<T>
    {
        private final T value;

        public ValueArg(T value)
        {
            this.value = value;
        }

        @Override
        public T getValue()
        {
            return value;
        }
    }

    public static final class NodeArg
            implements Arg<SNode>
    {
        private final SNode node;

        public NodeArg(SNode node)
        {
            this.node = checkNotNull(node);
        }

        @Override
        public SNode getValue()
        {
            return node;
        }
    }

    @SuppressWarnings({"rawtypes"})
    public interface Runtime<T>
    {
        boolean isTruthy(T object);

        ValueType getType(T object);

        default boolean isNull(T object)
        {
            return getType(object) == ValueType.NULL;
        }

        T createNull();

        T compare(SCompare.Op op, T left, T right);

        T createArray(List<T> items);

        T createObject(Map<String, T> fields);

        Iterable<T> toIterable(T object);

        T invokeFunction(String name, List<Arg> args);

        T createBoolean(boolean value);

        T getProperty(T object, String field);

        T parseString(String string);

        T createString(String value);

        T getVariable(int number);

        T getVariable(String name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class HeapRuntime
            implements Runtime<Object>
    {
        @Override
        public boolean isTruthy(Object object)
        {
            switch (getType(object)) {
                case NULL:
                    return false;
                case NUMBER:
                    return true;
                case STRING:
                    return !((String) object).isEmpty();
                case BOOLEAN:
                    return ((Boolean) object);
                case ARRAY:
                    return !((List) object).isEmpty();
                case OBJECT:
                    return !((Map) object).isEmpty();
                default:
                    throw new IllegalArgumentException(Objects.toString(object));
            }
        }

        @Override
        public ValueType getType(Object object)
        {
            if (object == null) {
                return ValueType.NULL;
            }
            else if (object instanceof Number) {
                return ValueType.NUMBER;
            }
            else if (object instanceof String) {
                return ValueType.STRING;
            }
            else if (object instanceof Boolean) {
                return ValueType.BOOLEAN;
            }
            else if (object instanceof List) {
                return ValueType.ARRAY;
            }
            else if (object instanceof Map) {
                return ValueType.OBJECT;
            }
            else {
                throw new IllegalArgumentException(Objects.toString(object));
            }
        }

        @Override
        public Object createNull()
        {
            return null;
        }

        @Override
        public Object compare(SCompare.Op op, Object left, Object right)
        {
            ValueType leftType = getType(left);
            ValueType rightType = getType(right);
            if (leftType == rightType) {
                switch (leftType) {
                    case NULL:
                        return 0;
                    case BOOLEAN:
                        return isTruthy(left) == isTruthy(right) ? 0 : -1;
                    case NUMBER:
                        return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
                    case STRING:
                        return ((String) left).compareTo((String) right);
                    case ARRAY:
                    case OBJECT:
                        // FIXME:
                        throw new UnsupportedOperationException();
                    default:
                        throw new IllegalArgumentException();
                }
            }
            else {
                return -1;
            }
        }

        @Override
        public Object createArray(List<Object> items)
        {
            return newArrayList(items);
        }

        @Override
        public Object createObject(Map<String, Object> fields)
        {
            return newLinkedHashMap(fields);
        }

        @Override
        public Iterable<Object> toIterable(Object object)
        {
            if (object instanceof List) {
                return newArrayList((List) object);
            }
            else if (object instanceof Map) {
                return newArrayList(((Map) object).values());
            }
            else {
                return newArrayList();
            }
        }

        @Override
        public Object invokeFunction(String name, List<Arg> args)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createBoolean(boolean value)
        {
            return value;
        }

        @Override
        public Object getProperty(Object object, String field)
        {
            if (object instanceof Map) {
                return ((Map) object).get(field);
            }
            else {
                return null;
            }
        }

        @Override
        public Object parseString(String string)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createString(String value)
        {
            return value;
        }

        @Override
        public Object getVariable(int number)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getVariable(String name)
        {
            throw new UnsupportedOperationException();
        }
    }

    public static <T> T evaluate(SNode search, Runtime<T> runtime, T object)
    {
        return search.accept(new SNodeVisitor<T, T>()
        {
            @Override
            public T visitAnd(SAnd node, T context)
            {
                T left = process(node.getLeft(), context);
                if (runtime.isTruthy(left)) {
                    return process(node.getRight(), context);
                }
                else {
                    return left;
                }
            }

            @Override
            public T visitCompare(SCompare node, T context)
            {
                T left = process(node.getLeft(), context);
                T right = process(node.getRight(), context);
                return runtime.compare(node.getOp(), left, right);
            }

            @Override
            public T visitCreateArray(SCreateArray node, T context)
            {
                if (runtime.isNull(context)) {
                    return context;
                }
                else {
                    return runtime.createArray(immutableMapItems(node.getItems(), n -> process(n, context)));
                }
            }

            @Override
            public T visitCreateObject(SCreateObject node, T context)
            {
                if (runtime.isNull(context)) {
                    return context;
                }
                else {
                    return runtime.createObject(immutableMapValues(node.getFields(), n -> process(n, context)));
                }
            }

            @Override
            public T visitCurrent(SCurrent node, T context)
            {
                return context;
            }

            @Override
            public T visitExpressionRef(SExpressionRef node, T context)
            {
                return process(node.getExpression(), context);
            }

            @Override
            public T visitFlattenArray(SFlattenArray node, T context)
            {
                if (runtime.getType(context) == ValueType.ARRAY) {
                    ImmutableList.Builder<T> builder = ImmutableList.builder();
                    for (T item : runtime.toIterable(context)) {
                        if (runtime.getType(item) == ValueType.ARRAY) {
                            builder.addAll(runtime.toIterable(item));
                        }
                        else {
                            builder.add(item);
                        }
                    }
                    return runtime.createArray(builder.build());
                }
                else {
                    return runtime.createNull();
                }
            }

            @Override
            public T visitFlattenObject(SFlattenObject node, T context)
            {
                if (runtime.getType(context) == ValueType.OBJECT) {
                    return runtime.createArray(ImmutableList.copyOf(runtime.toIterable(context)));
                }
                else {
                    return runtime.createNull();
                }
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public T visitFunctionCall(SFunctionCall node, T context)
            {
                List<Arg> args = node.getArgs().stream()
                        .map(arg -> {
                            if (arg instanceof SExpressionRef) {
                                return new NodeArg(arg);
                            }
                            else {
                                return new ValueArg(process(arg, context));
                            }
                        })
                        .collect(toImmutableList());
                return runtime.invokeFunction(node.getName(), args);
            }

            @Override
            public T visitIndex(SIndex node, T context)
            {
                if (runtime.getType(context) == ValueType.ARRAY) {
                    List<T> items = ImmutableList.copyOf(runtime.toIterable(context));
                    int i = node.getValue();
                    if (i < 0) {
                        i = items.size() + i;
                    }
                    if (i >= 0 && i < items.size()) {
                        return items.get(i);
                    }
                }
                return runtime.createNull();
            }

            @Override
            public T visitJsonLiteral(SJsonLiteral node, T context)
            {
                return runtime.parseString(node.getText());
            }

            @Override
            public T visitNegate(SNegate node, T context)
            {
                return runtime.createBoolean(runtime.isTruthy(process(node.getItem(), context)));
            }

            @Override
            public T visitOr(SOr node, T context)
            {
                T left = process(node.getLeft(), context);
                if (runtime.isTruthy(left)) {
                    return left;
                }
                else {
                    return process(node.getRight(), context);
                }
            }

            @Override
            public T visitProject(SProject node, T context)
            {
                if (runtime.getType(context) == ValueType.ARRAY) {
                    List<T> items = streamIterator(runtime.toIterable(context).iterator())
                            .map(v -> process(node.getChild(), v))
                            .filter(v -> !runtime.isNull(v))
                            .collect(toImmutableList());
                    return runtime.createArray(items);
                }
                else {
                    return runtime.createNull();
                }
            }

            @Override
            public T visitProperty(SProperty node, T context)
            {
                return runtime.getProperty(context, node.getName());
            }

            @Override
            public T visitSelection(SSelection node, T context)
            {
                if (runtime.getType(context) == ValueType.ARRAY) {
                    List<T> items = streamIterator(runtime.toIterable(context).iterator())
                            .filter(v -> runtime.isTruthy(process(node.getChild(), v)))
                            .collect(toImmutableList());
                    return runtime.createArray(items);
                }
                else {
                    return runtime.createNull();
                }
            }

            @Override
            public T visitSequence(SSequence node, T context)
            {
                for (SNode child : node.getItems()) {
                    context = process(child, context);
                }
                return context;
            }

            @Override
            public T visitSlice(SSlice node, T context)
            {
                List<T> items = ImmutableList.copyOf(runtime.toIterable(context));
                int step = node.getStep().orElse(1);
                int rounding = (step < 0) ? (step + 1) : (step - 1);
                int limit = (step < 0) ? -1 : 0;
                int start = node.getStart().orElse(limit);
                int stop = node.getStop().orElse(step < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
                int begin = (start < 0) ? Math.max(items.size() + start, 0) : Math.min(start, items.size() + limit);
                int end = (stop < 0) ? Math.max(items.size() + stop, limit) : Math.min(stop, items.size());
                int steps = Math.max(0, (end - begin + rounding) / step);
                ImmutableList.Builder<T> builder = ImmutableList.builder();
                for (int i = 0, offset = begin; i < steps; i++, offset += step) {
                    builder.add(items.get(offset));
                }
                return runtime.createArray(builder.build());
            }

            @Override
            public T visitString(SString node, T context)
            {
                return runtime.createString(node.getValue());
            }

            @Override
            public T visitParameter(SParameter node, T context)
            {
                if (node.getTarget() instanceof SParameter.NameTarget) {
                    return runtime.getVariable(((SParameter.NameTarget) node.getTarget()).getValue());
                }
                else if (node.getTarget() instanceof SParameter.NumberTarget) {
                    return runtime.getVariable(((SParameter.NumberTarget) node.getTarget()).getValue());
                }
                else {
                    throw new IllegalStateException(Objects.toString(node.getTarget()));
                }
            }
        }, object);
    }
}
