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
package com.wrmsr.tokamak.test.parse;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.AstBuilding;
import com.wrmsr.tokamak.core.parse.AstRendering;
import com.wrmsr.tokamak.core.parse.Parsing;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.transform.NameResolution;
import com.wrmsr.tokamak.core.parse.transform.SelectExpansion;
import com.wrmsr.tokamak.core.parse.transform.ViewInlining;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.test.TpchUtils;
import junit.framework.TestCase;

import java.nio.file.Path;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public class TpchParserTest
        extends TestCase
{
    public void testTpchParse()
            throws Throwable
    {
        Path tempDir = createTempDirectory();
        String url = "jdbc:h2:file:" + tempDir.toString() + "/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog catalog = TpchUtils.buildCatalog(url);
        Optional<String> defaultSchema = Optional.of("PUBLIC");

        for (String str : new String[] {
                "select * from NATION",
                "select * from NATION, NATION",
                "select N_NAME, N_COMMENT from NATION",
                "select N_NAME as name, N_COMMENT as comment from NATION",
                "select N_NAME, N_COMMENT from NATION as n",
                "select N_COMMENT, exclaim(N_NAME) from NATION",
                "select N_COMMENT, exclaim(exclaim(N_NAME)) from NATION",
        }) {
            SqlParser parser = Parsing.parse(str);
            TreeNode treeNode = AstBuilding.build(parser.statement());
            System.out.println(AstRendering.render(treeNode));

            treeNode = ViewInlining.inlineViews(treeNode, catalog);
            treeNode = SelectExpansion.expandSelects(treeNode, catalog, defaultSchema);
            System.out.println(AstRendering.render(treeNode));

            treeNode = NameResolution.resolveNames(treeNode, Optional.of(catalog), defaultSchema);
            System.out.println(AstRendering.render(treeNode));

            System.out.println();

            // Node node = new AstPlanner(Optional.of(catalog), defaultSchema).plan(treeNode);
            // Plan transformedPlan = Transforms.addScanNodeIdFields(new Plan(node), catalog);
        }
    }
}
