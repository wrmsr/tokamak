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
package com.wrmsr.tokamak.core.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class TypeConstruction
{
    private TypeConstruction()
    {
    }

    public static abstract class Supplier
    {
        private final String baseName;

        public Supplier(String baseName)
        {
            this.baseName = checkNotEmpty(baseName);
        }
    }

    public static final class BareSupplier
            extends Supplier
    {
        private final java.util.function.Supplier<Type> impl;

        public BareSupplier(String baseName, java.util.function.Supplier<Type> impl)
        {
            super(baseName);
            this.impl = checkNotNull(impl);
        }
    }

    public static final class ArgsSupplier
            extends Supplier
    {
        private final java.util.function.Function<List<Object>, Type> impl;
        private final OptionalInt arity;

        public ArgsSupplier(String baseName, Function<List<Object>, Type> impl, OptionalInt arity)
        {
            super(baseName);
            this.impl = checkNotNull(impl);
            this.arity = checkNotNull(arity);
        }
    }

    public static final class KwargsSupplier
            extends Supplier
    {
        private final java.util.function.Function<Map<String, Object>, Type> impl;

        public KwargsSupplier(String baseName, Function<Map<String, Object>, Type> impl)
        {
            super(baseName);
            this.impl = checkNotNull(impl);
        }
    }

    public static Type supplyParsedType(Supplier supplier, TypeParsing.ParsedType parsedType)
    {
        checkNotNull(parsedType);
        checkArgument(parsedType.getName().equals(supplier.baseName));
        if (supplier instanceof BareSupplier) {
            BareSupplier bareSupplier = (BareSupplier) supplier;
            checkArgument(parsedType.getItems().isEmpty());
            return bareSupplier.impl.get();
        }
        else if (supplier instanceof ArgsSupplier) {
            ArgsSupplier argsSupplier = (ArgsSupplier) supplier;
            List<Object> args = new ArrayList<>();
            for (TypeParsing.ArgOrKwarg aok : parsedType.getItems()) {
                checkArgument(!aok.getName().isPresent());
                args.add(aok.getValue());
            }
            argsSupplier.arity.ifPresent(a -> checkArgument(a == args.size()));
            return argsSupplier.impl.apply(args);
        }
        else if (supplier instanceof KwargsSupplier) {
            KwargsSupplier kwargsSupplier = (KwargsSupplier) supplier;
            Map<String, Object> kwargs = new HashMap<>();
            for (TypeParsing.ArgOrKwarg aok : parsedType.getItems()) {
                checkArgument(aok.getName().isPresent());
                String name = checkNotEmpty(aok.getName().get());
                checkArgument(!kwargs.containsKey(name));
                kwargs.put(name, aok.getValue());
            }
            return kwargsSupplier.impl.apply(kwargs);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(supplier));
        }
    }
}
