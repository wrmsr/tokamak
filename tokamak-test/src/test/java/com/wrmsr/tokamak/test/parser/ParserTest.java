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
package com.wrmsr.tokamak.test.parser;

import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.parser.AstBuilder;
import com.wrmsr.tokamak.parser.AstPlanner;
import com.wrmsr.tokamak.parser.Parsing;
import com.wrmsr.tokamak.parser.SqlParser;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import com.wrmsr.tokamak.test.TpchUtils;
import junit.framework.TestCase;

import java.util.Optional;

public class ParserTest
        extends TestCase
{
    public void testTpchParse()
            throws Throwable
    {
        TpchUtils.clearDatabase();
        String url = "jdbc:h2:file:./temp/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog catalog = TpchUtils.buildCatalog(url);

        for (String str : new String[] {
                "select * from NATION",
        }) {
            System.out.println(str);
            SqlParser parser = Parsing.parse(str);
            System.out.println(parser);
            TreeNode treeNode = new AstBuilder().build(parser.statement());
            System.out.println(treeNode);
            Node node = new AstPlanner(Optional.of(catalog), Optional.of("PUBLIC")).plan(treeNode);
            System.out.println(node);
        }
    }
}
