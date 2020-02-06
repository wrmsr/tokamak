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

package com.wrmsr.tokamak.core.shell;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.PlanningContext;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.transform.DropExposedInternalFieldsTransform;
import com.wrmsr.tokamak.core.plan.transform.MergeScansTransform;
import com.wrmsr.tokamak.core.plan.transform.PersistExposedTransform;
import com.wrmsr.tokamak.core.plan.transform.PersistScansTransform;
import com.wrmsr.tokamak.core.plan.transform.PropagateIdsTransform;
import com.wrmsr.tokamak.core.plan.transform.SetInvalidationsTransform;
import com.wrmsr.tokamak.core.tree.ParseOptions;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.TreeParsing;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.plan.TreePlanner;
import com.wrmsr.tokamak.core.tree.transform.SelectExpansion;
import com.wrmsr.tokamak.core.tree.transform.SymbolResolution;
import com.wrmsr.tokamak.core.tree.transform.ViewInlining;

import java.util.Optional;

public class TokamakShell
{
    private Catalog catalog;

    public TokamakShell(Catalog catalog)
    {
        this.catalog = catalog;
    }

    public Plan plan(String sql, ShellSession session)
    {
        ParsingContext parsingContext = new ParsingContext(
                new ParseOptions(),
                Optional.of(catalog),
                session.getDefaultSchema());

        SqlParser parser = buildParser(sql, parsingContext);

        TNode treeNode =  buildTree(parser, parsingContext);
        treeNode = rewriteTree(treeNode, parsingContext);

        Plan plan = buildPlan(treeNode, parsingContext);

        PlanningContext planningContext = new PlanningContext(
                Optional.of(catalog),
                Optional.of(parsingContext));

        plan = rewritePlan(plan, planningContext);

        return plan;
    }

    public SqlParser buildParser(String sql, ParsingContext parsingContext)
    {
        SqlParser parser = TreeParsing.parse(sql);
        parsingContext.setParser(parser);

        return parser;
    }

    public TNode buildTree(SqlParser parser, ParsingContext parsingContext)
    {
        TNode treeNode = TreeParsing.build(parser.statement());
        parsingContext.setOriginalTreeNode(treeNode);

        return treeNode;
    }

    public TNode rewriteTree(TNode treeNode, ParsingContext parsingContext)
    {
        treeNode = ViewInlining.inlineViews(treeNode, parsingContext);
        treeNode = SelectExpansion.expandSelects(treeNode, parsingContext);
        treeNode = SymbolResolution.resolveSymbols(treeNode, parsingContext);

        return treeNode;
    }

    public Plan buildPlan(TNode treeNode, ParsingContext parsingContext)
    {
        PNode node = new TreePlanner(parsingContext).plan(treeNode);
        return Plan.of(node);
    }

    public Plan rewritePlan(Plan plan, PlanningContext planningContext)
    {
        plan = MergeScansTransform.mergeScans(plan);
        plan = PersistScansTransform.persistScans(plan);
        plan = PersistExposedTransform.persistExposed(plan);
        plan = PropagateIdsTransform.propagateIds(plan, planningContext);
        plan = DropExposedInternalFieldsTransform.dropExposedInternalFields(plan);
        plan = SetInvalidationsTransform.setInvalidations(plan, planningContext);

        return plan;
    }
}
