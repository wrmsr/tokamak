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
package com.wrmsr.tokamak.parser.tree.visitor;

import com.wrmsr.tokamak.parser.tree.AllSelectItem;
import com.wrmsr.tokamak.parser.tree.Expression;
import com.wrmsr.tokamak.parser.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.parser.tree.Identifier;
import com.wrmsr.tokamak.parser.tree.IntegerLiteral;
import com.wrmsr.tokamak.parser.tree.Literal;
import com.wrmsr.tokamak.parser.tree.NullLiteral;
import com.wrmsr.tokamak.parser.tree.QualifiedName;
import com.wrmsr.tokamak.parser.tree.Relation;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SelectItem;
import com.wrmsr.tokamak.parser.tree.Statement;
import com.wrmsr.tokamak.parser.tree.StringLiteral;
import com.wrmsr.tokamak.parser.tree.TableName;
import com.wrmsr.tokamak.parser.tree.TreeNode;

import java.util.Objects;

public abstract class AstVisitor<R, C>
{
    private R visitTreeNode(TreeNode treeNode, C context)
    {
        throw new IllegalArgumentException(Objects.toString(treeNode));
    }

    public R visitAllSelectItem(AllSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitExpression(Expression treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitExpressionSelectItem(ExpressionSelectItem treeNode, C context)
    {
        return visitSelectItem(treeNode, context);
    }

    public R visitIdentifier(Identifier treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitIntegerLiteral(IntegerLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitLiteral(Literal treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitNullLiteral(NullLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitQualifiedName(QualifiedName treeNode, C context)
    {
        return visitExpression(treeNode, context);
    }

    public R visitRelation(Relation treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitSelect(Select treeNode, C context)
    {
        return visitStatement(treeNode, context);
    }

    public R visitSelectItem(SelectItem treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStatement(Statement treeNode, C context)
    {
        return visitTreeNode(treeNode, context);
    }

    public R visitStringLiteral(StringLiteral treeNode, C context)
    {
        return visitLiteral(treeNode, context);
    }

    public R visitTableName(TableName treeNode, C context)
    {
        return visitRelation(treeNode, context);
    }
}
