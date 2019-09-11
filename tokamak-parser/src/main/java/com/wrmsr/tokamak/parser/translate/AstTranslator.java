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

package com.wrmsr.tokamak.parser.translate;

import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import com.wrmsr.tokamak.parser.tree.visitor.AstVisitor;

public class AstTranslator
{
    public AstTranslator()
    {
    }

    public Node translate(TreeNode treeNode)
    {
        return treeNode.accept(new AstVisitor<Node, Void>()
        {
            // @Override
            // public Node visitSelect(Select treeNode, Void context)
            // {
            //     return new ScanNode(
            //
            //     );
            // }
        }, null);
    }
}
