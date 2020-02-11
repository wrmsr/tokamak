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

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;

public final class SymbolResolutions
{
    final Map<SymbolRef, Symbol> symbols;
    final Map<Symbol, Set<SymbolRef>> symbolRefs;

    public SymbolResolutions(Map<SymbolRef, Symbol> symbols, Map<Symbol, Set<SymbolRef>> symbolRefs)
    {
        this.symbols = ImmutableMap.copyOf(symbols);
        this.symbolRefs = newImmutableSetMap(symbolRefs);
    }

    public Map<SymbolRef, Symbol> getSymbols()
    {
        return symbols;
    }

    public Map<Symbol, Set<SymbolRef>> getSymbolRefs()
    {
        return symbolRefs;
    }
}
