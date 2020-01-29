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

package com.wrmsr.tokamak.core.plan.value.visitor;

import com.wrmsr.tokamak.core.plan.value.VArithmeticBinary;
import com.wrmsr.tokamak.core.plan.value.VArithmeticUnary;
import com.wrmsr.tokamak.core.plan.value.VConstant;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VLogicalBinary;
import com.wrmsr.tokamak.core.plan.value.VLogicalNot;
import com.wrmsr.tokamak.core.plan.value.VNode;

import java.util.Objects;

public abstract class VNodeVisitor<R, C>
{
    public R process(VNode node, C context)
    {
        return node.accept(this, context);
    }

    protected R visitNode(VNode node, C context)
    {
        throw new IllegalStateException(Objects.toString(node));
    }

    public R visitArithmeticBinary(VArithmeticBinary node, C context)
    {
        return visitNode(node, context);
    }

    public R visitArithmeticUnary(VArithmeticUnary node, C context)
    {
        return visitNode(node, context);
    }

    public R visitConstant(VConstant node, C context)
    {
        return visitNode(node, context);
    }

    public R visitField(VField node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFunction(VFunction node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLogicalBinary(VLogicalBinary node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLogicalNot(VLogicalNot node, C context)
    {
        return visitNode(node, context);
    }

}
