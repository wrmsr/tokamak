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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SymbolScope
{
    final TNode node;
    final Set<TNode> enclosedNodes = new LinkedHashSet<>();

    final Optional<SymbolScope> parent;
    final Set<SymbolScope> children = new LinkedHashSet<>();

    final Optional<String> name;

    final Set<Symbol> symbols = new LinkedHashSet<>();
    final Set<SymbolRef> symbolRefs = new LinkedHashSet<>();

    public SymbolScope(TNode node, Optional<SymbolScope> parent, Optional<String> name)
    {
        this.node = checkNotNull(node);
        this.parent = checkNotNull(parent);
        this.name = checkNotNull(name);

        parent.ifPresent(p -> p.children.add(this));
        enclosedNodes.add(node);
    }

    @Override
    public String toString()
    {
        return "Scope{" +
                "node=" + node +
                ", name=" + name +
                '}';
    }

    public TNode getNode()
    {
        return node;
    }

    public Set<TNode> getEnclosedNodes()
    {
        return Collections.unmodifiableSet(enclosedNodes);
    }

    public Optional<SymbolScope> getParent()
    {
        return parent;
    }

    public Set<SymbolScope> getChildren()
    {
        return Collections.unmodifiableSet(children);
    }

    public Optional<String> getName()
    {
        return name;
    }

    public Set<Symbol> getSymbols()
    {
        return Collections.unmodifiableSet(symbols);
    }

    public Set<SymbolRef> getSymbolRefs()
    {
        return Collections.unmodifiableSet(symbolRefs);
    }
}
