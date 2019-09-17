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
package com.wrmsr.tokamak.util.inject.advice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;

public abstract class AdvisesBinder
{
    private AdvisesBinder()
    {
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> type)
    {
        return newRealAdvisesBinder(binder, Key.get(type));
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, Class<T> type)
    {
        return newRealAdvisesBinder(binder, Key.get(type));
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> type, Annotation annotation)
    {
        return newRealAdvisesBinder(binder, Key.get(type, annotation));
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, Class<T> type, Annotation annotation)
    {
        return newRealAdvisesBinder(binder, Key.get(type, annotation));
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> type, Class<? extends Annotation> annotationType)
    {
        return newRealAdvisesBinder(binder, Key.get(type, annotationType));
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, Key<T> key)
    {
        return newRealAdvisesBinder(binder, key);
    }

    public static <T> LinkedBindingBuilder<T> bind(Binder binder, Class<T> type, Class<? extends Annotation> annotationType)
    {
        return newRealAdvisesBinder(binder, Key.get(type, annotationType));
    }

    static <T> Key<T> getAdvisesKeyForNewItem(Binder binder, Key<T> key)
    {
        binder = binder.skipSources(AdvisesBinder.class);

        Annotation annotation = key.getAnnotation();
        String elementName = key.hasAttributes() ? key.getAnnotation().toString() : "";
        AdviceElement element = new AdviceElementImpl(elementName, AdviceElement.Type.SOURCE, 0);
        Key<T> uniqueKey = Key.get(key.getTypeLiteral(), element);

        // Bind the original key to a new AdvisedProvider
        binder.bind(key).toProvider(new AdvisedProvider<T>(key.getTypeLiteral(), element.name(), annotation, binder.getProvider(uniqueKey)));

        return uniqueKey;
    }

    static <T> LinkedBindingBuilder<T> newRealAdvisesBinder(Binder binder, Key<T> key)
    {
        Key<T> uniqueKey = getAdvisesKeyForNewItem(binder, key);
        return binder.bind(uniqueKey);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, TypeLiteral<T> type, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type), order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, Class<T> type, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type), order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, TypeLiteral<T> type, Annotation annotation, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type, annotation), order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, Class<T> type, Annotation annotation, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type, annotation), order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, TypeLiteral<T> type, Class<? extends Annotation> annotationType, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type, annotationType), order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, Key<T> key, int order)
    {
        return newRealAdviceBinder(binder, key, order);
    }

    public static <T> LinkedBindingBuilder<UnaryOperator<T>> bindAdvice(Binder binder, Class<T> type, Class<? extends Annotation> annotationType, int order)
    {
        return newRealAdviceBinder(binder, Key.get(type, annotationType), order);
    }

    @SuppressWarnings("unchecked")
    static <T> Key<UnaryOperator<T>> getAdviceKeyForNewItem(Binder binder, Key<T> key, int order)
    {
        binder = binder.skipSources(AdvisesBinder.class);
        String elementName = key.hasAttributes() ? key.getAnnotation().toString() : "";
        @SuppressWarnings("unused")
        Annotation annotation = key.getAnnotation();

        Type adviceType = Types.newParameterizedType(UnaryOperator.class, key.getTypeLiteral().getType());
        return (Key<UnaryOperator<T>>) Key.get(adviceType, new AdviceElementImpl(elementName, AdviceElement.Type.ADVICE, order));
    }

    static <T> LinkedBindingBuilder<UnaryOperator<T>> newRealAdviceBinder(Binder binder, Key<T> key, int order)
    {
        Key<UnaryOperator<T>> uniqueKey = getAdviceKeyForNewItem(binder, key, order);
        return binder.bind(uniqueKey);
    }
}
