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

import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeLike;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@FunctionalInterface
public interface TypeConstructor
{
    TypeLike construct(List<Object> args, Map<String, Object> kwargs);

    static TypeConstructor of(TypeConstructor fn)
    {
        return checkNotNull(fn);
    }

    @FunctionalInterface
    interface Nullary
    {
        TypeLike construct();
    }

    static TypeConstructor of(Nullary fn)
    {
        return (args, kwargs) -> {
            checkArgument(args.isEmpty());
            checkArgument(kwargs.isEmpty());
            return fn.construct();
        };
    }

    @FunctionalInterface
    interface Unary
    {
        Type construct(Type type0);
    }

    static TypeConstructor of(Unary fn)
    {
        return (args, kwargs) -> {
            checkArgument(args.size() == 1);
            checkArgument(kwargs.isEmpty());
            return fn.construct((Type) args.get(0));
        };
    }

    @FunctionalInterface
    interface Binary
    {
        Type construct(Type type0, Type type1);
    }

    static TypeConstructor of(Binary fn)
    {
        return (args, kwargs) -> {
            checkArgument(args.size() == 2);
            checkArgument(kwargs.isEmpty());
            return fn.construct((Type) args.get(0), (Type) args.get(1));
        };
    }

    @FunctionalInterface
    interface Args
    {
        TypeLike construct(List<Object> args);
    }

    static TypeConstructor of(Args fn)
    {
        return (args, kwargs) -> {
            checkArgument(kwargs.isEmpty());
            return fn.construct(args);
        };
    }

    @FunctionalInterface
    interface Kwargs
    {
        TypeLike construct(Map<String, Object> kwargs);
    }

    static TypeConstructor of(Kwargs fn)
    {
        return (args, kwargs) -> {
            checkArgument(args.isEmpty());
            return fn.construct(kwargs);
        };
    }

    /*
    public static Type supplyParsedType(Supplier supplier, TypeParsing.ParsedType parsedType)
    {
        checkNotNull(parsedType);
        checkArgument(parsedType.getName().equals(supplier.name));
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
    */
}
