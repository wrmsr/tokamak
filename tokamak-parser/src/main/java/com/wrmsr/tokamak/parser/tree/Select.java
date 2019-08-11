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

package com.wrmsr.tokamak.parser.tree;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class Select
        extends Statement
{
    public abstract class Item
    {
    }

    public final class All
            extends Item
    {
    }

    public final class Column
            extends Item
    {
    }

    private final List<Item> items;

    public Select(List<Item> items)
    {
        this.items = ImmutableList.copyOf(items);
    }

    public List<Item> getItems()
    {
        return items;
    }
}
