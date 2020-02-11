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
package com.wrmsr.tokamak.core.tree.analysis.symbol;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.tree.node.TNode;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SymbolRef
{
    final Optional<List<String>> nameParts;
    final TNode node;
    final Optional<Symbol> binding;
    final SymbolScope symbolScope;

    public SymbolRef(Optional<List<String>> nameParts, TNode node, Optional<Symbol> binding, SymbolScope symbolScope)
    {
        this.nameParts = checkNotNull(nameParts).map(ImmutableList::copyOf);
        this.node = checkNotNull(node);
        this.binding = checkNotNull(binding);
        this.symbolScope = checkNotNull(symbolScope);

        symbolScope.symbolRefs.add(this);
    }

    @Override
    public String toString()
    {
        return "SymbolRef{" +
                "nameParts=" + nameParts +
                '}';
    }

    public Optional<List<String>> getNameParts()
    {
        return nameParts;
    }

    public TNode getNode()
    {
        return node;
    }

    public Optional<Symbol> getBinding()
    {
        return binding;
    }

    public SymbolScope getSymbolScope()
    {
        return symbolScope;
    }
}
