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
package com.wrmsr.tokamak.core.plan.node.visitor;

import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PExtract;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnify;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;

import java.util.Objects;

public abstract class PNodeVisitor<R, C>
{
    public R process(PNode node, C context)
    {
        return node.accept(this, context);
    }

    protected R visitNode(PNode node, C context)
    {
        throw new IllegalStateException(Objects.toString(node));
    }

    public R visitCache(PCache node, C context)
    {
        return visitNode(node, context);
    }

    public R visitExtract(PExtract node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFilter(PFilter node, C context)
    {
        return visitNode(node, context);
    }

    public R visitGroup(PGroup node, C context)
    {
        return visitNode(node, context);
    }

    public R visitJoin(PJoin node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLookup(PLookup node, C context)
    {
        return visitNode(node, context);
    }

    public R visitOutput(POutput node, C context)
    {
        return visitNode(node, context);
    }

    public R visitProject(PProject node, C context)
    {
        return visitNode(node, context);
    }

    public R visitScan(PScan node, C context)
    {
        return visitNode(node, context);
    }

    public R visitScope(PScope node, C context)
    {
        return visitNode(node, context);
    }

    public R visitScopeExit(PScopeExit node, C context)
    {
        return visitNode(node, context);
    }

    public R visitSearch(PSearch node, C context)
    {
        return visitNode(node, context);
    }

    public R visitState(PState node, C context)
    {
        return visitNode(node, context);
    }

    public R visitStruct(PStruct node, C context)
    {
        return visitNode(node, context);
    }

    public R visitUnify(PUnify node, C context)
    {
        return visitNode(node, context);
    }

    public R visitUnion(PUnion node, C context)
    {
        return visitNode(node, context);
    }

    public R visitUnnest(PUnnest node, C context)
    {
        return visitNode(node, context);
    }

    public R visitValues(PValues node, C context)
    {
        return visitNode(node, context);
    }
}
