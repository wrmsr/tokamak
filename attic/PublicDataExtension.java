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
package com.wrmsr.tokamak.util.derive4j;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;
import org.derive4j.processor.api.DeriveResult;
import org.derive4j.processor.api.DeriveUtils;
import org.derive4j.processor.api.Extension;
import org.derive4j.processor.api.ExtensionFactory;
import org.derive4j.processor.api.TypeSpecModifier;
import org.derive4j.processor.api.model.DataConstructor;

import javax.lang.model.element.Modifier;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(ExtensionFactory.class)
public final class PublicDataExtension implements ExtensionFactory
{
    @Override
    public List<Extension> extensions(DeriveUtils deriveUtils) {
        return singletonList((adtModel, codeGenSpec) -> {
            Set<String> strictConstructors = adtModel.dataConstruction()
                    .constructors()
                    .stream()
                    .map(DataConstructor::name)
                    .map(String::toLowerCase)
                    .collect(toSet());

            return DeriveResult.result(new TypeSpecModifier(codeGenSpec).modTypes(typeSpecs ->
                    typeSpecs.stream().map(ts ->
                            strictConstructors.contains(ts.name.toLowerCase())
                                    ? removePrivateModifier(ts)
                                    : ts)
                            .collect(toList())).build());

        });
    }

    private static TypeSpec removePrivateModifier(TypeSpec ts) {
        return new TypeSpecModifier(ts).modModifiers(
                modifiers -> modifiers.stream().filter(m -> m != Modifier.PRIVATE).collect(toSet())
        ).build();
    }
}

