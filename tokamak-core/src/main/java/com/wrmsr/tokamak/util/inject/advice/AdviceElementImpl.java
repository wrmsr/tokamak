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

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;

class AdviceElementImpl
        implements AdviceElement
{
    private static final AtomicInteger counter = new AtomicInteger();
    private final int id = counter.incrementAndGet();
    private final String name;
    private final Type type;
    private final int order;

    public AdviceElementImpl(String name, Type type, int order)
    {
        this.name = name;
        this.type = type;
        this.order = order;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return AdviceElement.class;
    }

    public int getOrder()
    {
        return order;
    }

    @Override
    public int uniqueId()
    {
        return id;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public Type type()
    {
        return type;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof AdviceElement
                && ((AdviceElement) o).name().equals(name())
                && ((AdviceElement) o).uniqueId() == uniqueId()
                && ((AdviceElement) o).type() == type();
    }

    @Override
    public int hashCode()
    {
        return ((127 * "name".hashCode()) ^ name().hashCode())
                + ((127 * "id".hashCode()) ^ uniqueId())
                + ((127 * "type".hashCode()) ^ type.hashCode());
    }

    public String toString()
    {
        return "@" + getClass().getSimpleName()
                + "(name=" + name()
                + ", type=" + type()
                + ", id=" + uniqueId() + ")";
    }
}
