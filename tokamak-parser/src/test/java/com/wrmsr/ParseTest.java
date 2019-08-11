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
package com.wrmsr;

import com.wrmsr.tokamak.parser.SqlLexer;
import com.wrmsr.tokamak.parser.SqlParser;
import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ParseTest
        extends TestCase
{
    public void testParse()
            throws Throwable
    {
        for (String str : new String[] {
                "commit",
                "select 420",
                "select /*+ hint */ 1",
                "select a from a",
        }) {
            CodePointCharStream input = CharStreams.fromString(str);

            SqlLexer lexer = new SqlLexer(input);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            SqlParser parser = new SqlParser(tokens);

            ParseTree tree = parser.statement();
            System.out.println(tree.toStringTree(parser));
        }
    }
}
