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

import com.wrmsr.tokamak.parser.CaseInsensitiveCharStream;
import com.wrmsr.tokamak.parser.SqlBaseVisitor;
import com.wrmsr.tokamak.parser.SqlLexer;
import com.wrmsr.tokamak.parser.SqlParser;
import com.wrmsr.tokamak.parser.tree.AllSelectItem;
import com.wrmsr.tokamak.parser.tree.Expression;
import com.wrmsr.tokamak.parser.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.parser.tree.IntegerLiteral;
import com.wrmsr.tokamak.parser.tree.NullLiteral;
import com.wrmsr.tokamak.parser.tree.QualifiedName;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SelectItem;
import com.wrmsr.tokamak.parser.tree.StringLiteral;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public class ParseTest
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
        }) {

            System.out.println(node);
        }
    }
}
