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
package com.wrmsr.tokamak.core.plan.node.annotation;

import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.json.Json;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreFunctions.tryGetMethodHandle;
import static com.wrmsr.tokamak.util.func.ThrowableThrowingSupplier.throwableRethrowingGet;

public final class PNodeAnnotations
{
    private PNodeAnnotations()
    {
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
