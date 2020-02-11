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

import com.wrmsr.tokamak.core.tree.node.TNode;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Symbol
{
    final Optional<String> name;
    final SymbolScope symbolScope;
    final Optional<Symbol> origin;
    final TNode node;

    public Symbol(Optional<String> name, TNode node, Optional<Symbol> origin, SymbolScope symbolScope)
    {
        this.name = checkNotNull(name);
        this.symbolScope = checkNotNull(symbolScope);
        this.origin = checkNotNull(origin);
        this.node = checkNotNull(node);

        symbolScope.symbols.add(this);
    }

    @Override
    public String toString()
    {
        return "Symbol{" +
                "name=" + name +
                '}';
    }

    public Optional<String> getName()
    {
        return name;
    }

    public SymbolScope getSymbolScope()
    {
        return symbolScope;
    }

    public Optional<Symbol> getOrigin()
    {
        return origin;
    }

    public TNode getNode()
    {
        return node;
    }
}
