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
package com.wrmsr.tokamak.parse;

import com.wrmsr.tokamak.parse.tree.TreeNode;
import junit.framework.TestCase;

public class ParserTest
        extends TestCase
{
    public void testParse()
            throws Throwable
    {
        for (String str : new String[] {
                "select *",
                "select 420",
                "select /*+ hint */ 1",
                "select 'hu'",
                "select a",
                "select a from a",
                "select a() as c, d from e",
                "select a(b) as c, d from e",
                "select a(b, x) as c, d from e",
                "select a // comment",
                "select a -- comment",
        }) {
            System.out.println(str);
            SqlParser parser = Parsing.parse(str);
            System.out.println(parser);
            TreeNode treeNode = new AstBuilder().build(parser.statement());
            System.out.println(treeNode);
        }
    }
}
