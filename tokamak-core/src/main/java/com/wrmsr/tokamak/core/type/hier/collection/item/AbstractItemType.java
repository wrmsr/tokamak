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
package com.wrmsr.tokamak.core.type.hier.collection.item;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class AbstractItemType
        implements ItemType
{
    private final Type item;

    public AbstractItemType(Type item)
    {
        this.item = checkNotNull(item);
    }

    @Override
    public List<Object> getTypeArgs()
    {
        return ImmutableList.of(item);
    }

    @Override
    public Type getItem()
    {
        return item;
    }
}
