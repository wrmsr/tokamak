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

import com.wrmsr.tokamak.core.plan.value.VConstant;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.plan.value.VSpecial;

import java.util.Map;

import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public class VNodeRewriter<C>
        extends CachingVNodeVisitor<VNode, C>
{
    public VNodeRewriter()
    {
    }

    public VNodeRewriter(Map<VNode, VNode> cache)
    {
        super(cache);
    }

    @Override
    protected VNode visitNode(VNode node, C context)
    {
        throw new IllegalStateException();
    }

    @Override
    public VNode visitConstant(VConstant node, C context)
    {
        return new VConstant(
                node.getValue(),
                node.getType());
    }

    @Override
    public VNode visitField(VField node, C context)
    {
        return new VField(
                node.getField());
    }

    @Override
    public VNode visitFunction(VFunction node, C context)
    {
        return new VFunction(
                node.getFunction(),
                immutableMapItems(node.getArgs(), a -> process(a, context)));
    }

    @Override
    public VNode visitSpecial(VSpecial node, C context)
    {
        return new VSpecial(
                node.getOp(),
                immutableMapItems(node.getArgs(), a -> process(a, context)));
    }
}
