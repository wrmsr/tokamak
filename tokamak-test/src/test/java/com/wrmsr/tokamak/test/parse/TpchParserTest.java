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

import com.wrmsr.tokamak.core.parse.AstAnalysis;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.parse.AstBuilder;
import com.wrmsr.tokamak.core.parse.AstPlanner;
import com.wrmsr.tokamak.core.parse.Parsing;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.transform.Transforms;
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

        for (String str : new String[] {
                "select * from NATION",
                "select N_NAME, N_COMMENT from NATION",
                "select N_NAME as name, N_COMMENT as comment from NATION",
                "select N_COMMENT, exclaim(N_NAME) from NATION",
                "select N_COMMENT, exclaim(exclaim(N_NAME)) from NATION",
        }) {
            SqlParser parser = Parsing.parse(str);
            TreeNode treeNode = new AstBuilder().build(parser.statement());

            AstAnalysis.analyze(treeNode, catalog);

            Node node = new AstPlanner(Optional.of(catalog), Optional.of("PUBLIC")).plan(treeNode);
            Plan transformedPlan = Transforms.addScanNodeIdFields(new Plan(node), catalog);
        }
    }
}
