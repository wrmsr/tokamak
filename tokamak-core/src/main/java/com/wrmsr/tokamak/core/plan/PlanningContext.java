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
package com.wrmsr.tokamak.core.plan;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.node.TNode;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PlanningContext
{
    private final Optional<Catalog> catalog;
    private final Optional<ParsingContext> parsingContext;
    private final Optional<TNode> treeNode;

    public PlanningContext(
            Optional<Catalog> catalog,
            Optional<ParsingContext> parsingContext,
            Optional<TNode> treeNode)
    {
        this.catalog = checkNotNull(catalog);
        this.parsingContext = checkNotNull(parsingContext);
        this.treeNode = checkNotNull(treeNode);
    }

    public Optional<Catalog> getCatalog()
    {
        return catalog;
    }

    public Optional<ParsingContext> getParsingContext()
    {
        return parsingContext;
    }

    public Optional<TNode> getTreeNode()
    {
        return treeNode;
    }
}
