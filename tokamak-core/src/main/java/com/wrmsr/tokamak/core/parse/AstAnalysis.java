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

package com.wrmsr.tokamak.core.parse;

import com.fasterxml.jackson.core.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstVisitor;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class AstAnalysis
{
    private AstAnalysis()
    {
    }

    public static final class Scope
    {
        private final TreeNode root;

        private final Scope parent;
        private final Set<Scope> children = new LinkedHashSet<>();

        private final Optional<String> name;
        private final Set<String> providedNames = new LinkedHashSet<>();

        public Scope(TreeNode root, Scope parent, Optional<String> name)
        {
            this.root = root;
            this.parent = parent;
            this.name = name;
        }
    }

    public static final class ScopeAstVisitor
            extends AstVisitor<Scope, Scope>
    {
        @Override
        public Scope visitSelect(Select treeNode, Scope context)
        {
            return super.visitSelect(treeNode, context);
        }
    }
}
