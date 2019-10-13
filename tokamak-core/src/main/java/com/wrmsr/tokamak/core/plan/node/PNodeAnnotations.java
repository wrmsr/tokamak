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
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.json.Json;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.func.ThrowableThrowingSupplier.throwableRethrowingGet;

@Immutable
public final class PNodeAnnotations
        extends AnnotationCollection<PNodeAnnotation, PNodeAnnotations>
{
    @Immutable
    public static final class Fields
            extends AnnotationCollectionMap<String, FieldAnnotation, Fields.Entry, Fields>
    {
        @Immutable
        public static final class Entry
                extends AnnotationCollectionMap.Entry<String, FieldAnnotation, PNodeAnnotations.Fields.Entry>
        {
            @JsonCreator
            public Entry(
                    @JsonProperty("key") String key,
                    @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
            {
                super(checkNotEmpty(key), annotations);
            }

            @JsonProperty("key")
            @Override
            public String getKey()
            {
                return super.getKey();
            }

            @JsonProperty("annotations")
            @Override
            public List<FieldAnnotation> get()
            {
                return super.get();
            }

            @Override
            public Class<FieldAnnotation> getBaseCls()
            {
                return FieldAnnotation.class;
            }

            @Override
            protected Entry rebuildWith(Iterable<FieldAnnotation> annotations)
            {
                return new Entry(key, annotations);
            }
        }

        @JsonCreator
        public Fields(
                @JsonProperty("entries") Iterable<Entry> entries)
        {
            super(entries);
        }

        @JsonProperty("entries")
        @Override
        public List<Entry> getEntries()
        {
            return super.getEntries();
        }

        @Override
        protected Fields rebuildWithEntries(Iterable<Entry> entries)
        {
            return new Fields(entries);
        }

        @Override
        protected Entry newEntry(String key, Iterable<FieldAnnotation> annotations)
        {
            return new Entry(key, annotations);
        }
    }

    private final Fields fields;

    @JsonCreator
    public PNodeAnnotations(
            @JsonProperty("annotations") Iterable<PNodeAnnotation> annotations,
            @JsonProperty("fields") Fields fields)
    {
        super(annotations);

        this.fields = checkNotNull(fields);
    }

    @JsonProperty("annotations")
    @Override
    public List<PNodeAnnotation> get()
    {
        return super.get();
    }

    @JsonProperty("fields")
    public Fields getFields()
    {
        return fields;
    }

    private PNodeAnnotations()
    {
        this(ImmutableList.of(), new Fields(ImmutableList.of()));
    }

    private static final PNodeAnnotations EMPTY = new PNodeAnnotations();

    public static PNodeAnnotations empty()
    {
        return EMPTY;
    }

    @Override
    public Class<PNodeAnnotation> getBaseCls()
    {
        return PNodeAnnotation.class;
    }

    @Override
    protected PNodeAnnotations rebuildWith(Iterable<PNodeAnnotation> annotations)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations withFields(Fields fields)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations mapFields(Function<Fields, Fields> fn)
    {
        return withFields(fn.apply(fields));
    }

    private static final SupplierLazyValue<Map<Class<? extends PNodeAnnotation>, Consumer<PNode>>> validatorsByAnnotationType = new SupplierLazyValue<>();

    public static Map<Class<? extends PNodeAnnotation>, Consumer<PNode>> getValidatorsByAnnotationType()
    {
        return validatorsByAnnotationType.get(() ->
                Json.getAnnotatedSubtypes(PNodeAnnotation.class).values().stream()
                        .<Optional<Pair<Class<? extends PNodeAnnotation>, Consumer<PNode>>>>map(cls -> {
                            try {
                                Method method = cls.getDeclaredMethod("validate", PNode.class);
                                MethodHandle handle = MethodHandles.lookup().unreflect(method);
                                Consumer<PNode> validator = node -> throwableRethrowingGet(() -> handle.invoke(node));
                                return Optional.of(Pair.immutable(cls, validator));
                            }
                            catch (NoSuchMethodException e) {
                                return Optional.empty();
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toImmutableMap()));
    }

    public static void validate(PNode node)
    {
        node.getAnnotations().forEach(annotation ->
                Optional.ofNullable(getValidatorsByAnnotationType().get(annotation.getClass()))
                        .ifPresent(validator -> validator.accept(node)));
    }
}
