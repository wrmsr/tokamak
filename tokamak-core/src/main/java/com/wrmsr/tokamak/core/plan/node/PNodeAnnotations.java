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
import com.wrmsr.tokamak.core.layout.field.FieldAnnotations;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.json.Json;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreFunctions.tryGetMethodHandle;
import static com.wrmsr.tokamak.util.func.ThrowableThrowingSupplier.throwableRethrowingGet;

@Immutable
public final class PNodeAnnotations
        extends AnnotationCollection<PNodeAnnotation, PNodeAnnotations>
{
    private final AnnotationCollectionMap<String, FieldAnnotation, FieldAnnotations> fields;

    @JsonCreator
    public PNodeAnnotations(
            @JsonProperty("annotations") Iterable<PNodeAnnotation> annotations,
            @JsonProperty("fields") Map<String, Iterable<FieldAnnotation>> fields)
    {
        super(annotations);

        this.fields = new AnnotationCollectionMap<>(immutableMapValues(checkNotNull(fields), FieldAnnotations::new));
    }

    @JsonProperty("annotations")
    @Override
    public List<PNodeAnnotation> get()
    {
        return super.get();
    }

    @JsonProperty("fields")
    public PNodeFieldAnnotations getFields()
    {
        return fields;
    }

    private PNodeAnnotations()
    {
        this(ImmutableList.of(), new PNodeFieldAnnotations(ImmutableList.of()));
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
    protected PNodeAnnotations rebuild(Iterable<PNodeAnnotation> annotations)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations withFields(PNodeFieldAnnotations fields)
    {
        return new PNodeAnnotations(annotations, fields);
    }

    public PNodeAnnotations mapFields(Function<PNodeFieldAnnotations, PNodeFieldAnnotations> fn)
    {
        return withFields(fn.apply(fields));
    }

    private static final SupplierLazyValue<Map<Class<? extends PNodeAnnotation>, Consumer<PNode>>> validatorsByAnnotationType = new SupplierLazyValue<>();

    public static Map<Class<? extends PNodeAnnotation>, Consumer<PNode>> getValidatorsByAnnotationType()
    {
        return validatorsByAnnotationType.get(() ->
                Json.getAnnotatedSubtypes(PNodeAnnotation.class).values().stream()
                        .<Optional<Pair<Class<? extends PNodeAnnotation>, Consumer<PNode>>>>map(cls ->
                                tryGetMethodHandle(cls, "validate", PNode.class).map(handle ->
                                        Pair.immutable(cls, node -> throwableRethrowingGet(() -> handle.invoke(node)))))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toImmutableMap()));
    }
}
