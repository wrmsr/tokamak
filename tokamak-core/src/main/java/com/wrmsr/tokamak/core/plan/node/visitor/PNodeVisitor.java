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
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroupBy;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;

import java.util.Objects;

public abstract class PNodeVisitor<R, C>
{
    protected R visitNode(PNode node, C context)
    {
        throw new IllegalStateException(Objects.toString(node));
    }

    public R visitCache(PCache node, C context)
    {
        return visitNode(node, context);
    }

    public R visitCrossJoin(PCrossJoin node, C context)
    {
        return visitNode(node, context);
    }

    public R visitEquiJoin(PEquiJoin node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFilter(PFilter node, C context)
    {
        return visitNode(node, context);
    }

    public R visitGroupBy(PGroupBy node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLookupJoin(PLookupJoin node, C context)
    {
        return visitNode(node, context);
    }

    public R visitState(PState node, C context)
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

    public R visitStruct(PStruct node, C context)
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
