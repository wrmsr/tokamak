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
package com.wrmsr.tokamak.core.search.node.visitor;

import com.wrmsr.tokamak.core.search.node.SAnd;
import com.wrmsr.tokamak.core.search.node.SCompare;
import com.wrmsr.tokamak.core.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search.node.SCurrent;
import com.wrmsr.tokamak.core.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search.node.SFlattenArray;
import com.wrmsr.tokamak.core.search.node.SFlattenObject;
import com.wrmsr.tokamak.core.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search.node.SIndex;
import com.wrmsr.tokamak.core.search.node.SJsonLiteral;
import com.wrmsr.tokamak.core.search.node.SNegate;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.SSlice;
import com.wrmsr.tokamak.core.search.node.SString;
import com.wrmsr.tokamak.core.search.node.SParameter;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

public class SNodeRewriter<C>
        extends CachingSNodeVisitor<SNode, C>
{
    @Override
    public SNode visitAnd(SAnd node, C context)
    {
        return new SAnd(
                process(node.getLeft(), context),
                process(node.getRight(), context));
    }

    @Override
    public SNode visitCompare(SCompare node, C context)
    {
        return new SCompare(
                node.getOp(),
                process(node.getLeft(), context),
                process(node.getRight(), context));
    }

    @Override
    public SNode visitCreateArray(SCreateArray node, C context)
    {
        return new SCreateArray(
                node.getItems().stream().map(n -> process(n, context)).collect(toImmutableList()));
    }

    @Override
    public SNode visitCreateObject(SCreateObject node, C context)
    {
        return new SCreateObject(
                immutableMapValues(node.getFields(), n -> process(n, context)));
    }

    @Override
    public SNode visitCurrent(SCurrent node, C context)
    {
        return new SCurrent();
    }

    @Override
    public SNode visitExpressionRef(SExpressionRef node, C context)
    {
        return new SExpressionRef(
                process(node.getExpression(), context));
    }

    @Override
    public SNode visitFlattenArray(SFlattenArray node, C context)
    {
        return new SFlattenArray();
    }

    @Override
    public SNode visitFlattenObject(SFlattenObject node, C context)
    {
        return new SFlattenObject();
    }

    @Override
    public SNode visitFunctionCall(SFunctionCall node, C context)
    {
        return new SFunctionCall(
                node.getName(),
                node.getArgs().stream().map(n -> process(n, context)).collect(toImmutableList()));
    }

    @Override
    public SNode visitIndex(SIndex node, C context)
    {
        return new SIndex(
                node.getValue());
    }

    @Override
    public SNode visitJsonLiteral(SJsonLiteral node, C context)
    {
        return new SJsonLiteral(
                node.getText());
    }

    @Override
    public SNode visitNegate(SNegate node, C context)
    {
        return new SNegate(
                process(node.getItem(), context));
    }

    @Override
    public SNode visitOr(SOr node, C context)
    {
        return new SOr(
                process(node.getLeft(), context),
                process(node.getRight(), context));
    }

    @Override
    public SNode visitParameter(SParameter node, C context)
    {
        return new SParameter(
                node.getTarget());
    }

    @Override
    public SNode visitProject(SProject node, C context)
    {
        return new SProject(
                process(node.getChild(), context));
    }

    @Override
    public SNode visitProperty(SProperty node, C context)
    {
        return new SProperty(
                node.getName());
    }

    @Override
    public SNode visitSelection(SSelection node, C context)
    {
        return new SSelection(
                process(node.getChild(), context));
    }

    @Override
    public SNode visitSequence(SSequence node, C context)
    {
        return new SSequence(
                node.getItems().stream().map(n -> process(n, context)).collect(toImmutableList()));
    }

    @Override
    public SNode visitSlice(SSlice node, C context)
    {
        return new SSlice(
                node.getStart(),
                node.getStop(),
                node.getStep());
    }

    @Override
    public SNode visitString(SString node, C context)
    {
        return new SString(
                node.getValue());
    }
}
