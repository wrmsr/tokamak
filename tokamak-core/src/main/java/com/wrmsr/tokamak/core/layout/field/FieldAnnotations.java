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
package com.wrmsr.tokamak.core.layout.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
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

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.func.ThrowableThrowingSupplier.throwableRethrowingGet;

@Immutable
public final class FieldAnnotations
        extends AnnotationCollection<FieldAnnotation, FieldAnnotations>
{
    @JsonCreator
    public FieldAnnotations(
            @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
    {
        super(annotations);
    }

    public static FieldAnnotations of(FieldAnnotation... annotations)
    {
        return new FieldAnnotations(ImmutableList.copyOf(annotations));
    }

    @JsonProperty("annotations")
    @Override
    public List<FieldAnnotation> get()
    {
        return super.get();
    }

    private FieldAnnotations()
    {
        this(ImmutableList.of());
    }

    private static final FieldAnnotations EMPTY = new FieldAnnotations();

    public static FieldAnnotations empty()
    {
        return EMPTY;
    }

    @Override
    public Class<FieldAnnotation> getBaseCls()
    {
        return FieldAnnotation.class;
    }

    @Override
    protected FieldAnnotations rebuildWith(Iterable<FieldAnnotation> annotations)
    {
        return new FieldAnnotations(annotations);
    }

    private static final SupplierLazyValue<Map<Class<? extends FieldAnnotation>, Consumer<Field>>> validatorsByAnnotationType = new SupplierLazyValue<>();

    public static Map<Class<? extends FieldAnnotation>, Consumer<Field>> getValidatorsByAnnotationType()
    {
        return validatorsByAnnotationType.get(() -> {
            return Json.getAnnotatedSubtypes(FieldAnnotation.class).values().stream()
                    .map(cls -> {
                        try {
                            Method method = cls.getDeclaredMethod("validate", Field.class);
                            MethodHandle handle = MethodHandles.lookup().unreflect(method);
                            Consumer<Field> validator = field -> throwableRethrowingGet(() -> method.invoke(field));
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
                    .collect(toImmutableMap());
        });
    }
}
