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
package com.wrmsr.tokamak.core.parse.transform;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstRewriter;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NameResolution
{
    private NameResolution()
    {
    }

    public static TreeNode resolveNames(TreeNode node, Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        ScopeAnalysis sa = ScopeAnalysis.analyze(node, catalog, defaultSchema);

        return node.accept(new AstRewriter<Void>()
        {
            @Override
            public TreeNode visitAllSelectItem(AllSelectItem treeNode, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public TreeNode visitQualifiedName(QualifiedName treeNode, Void context)
            {
                ScopeAnalysis.SymbolRef sr = checkNotNull(sa.getSymbolRefsByNode().get(treeNode));
                // List<ScopeAnalysis.Scope> hits = sr.getScope().getChildren().stream()
                //         .filter(s -> s.getSymbols().stream().anyMatch(s -> s.getName().isPresent() && s.getName().get().equals()))
                return super.visitQualifiedName(treeNode, context);
            }
        }, null);
    }
}
