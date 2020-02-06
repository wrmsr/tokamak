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
package com.wrmsr.tokamak.core.tree;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.util.Cell;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ParsingContext
{
    private final ParseOptions parseOptions;
    private final Optional<Catalog> catalog;
    private final Optional<String> defaultSchema;

    public ParsingContext(
            ParseOptions parseOptions,
            Optional<Catalog> catalog,
            Optional<String> defaultSchema)
    {
        this.parseOptions = checkNotNull(parseOptions);
        this.catalog = checkNotNull(catalog);
        this.defaultSchema = checkNotNull(defaultSchema);
    }

    public ParsingContext(ParseOptions parseOptions)
    {
        this(
                parseOptions,
                Optional.empty(),
                Optional.empty());
    }

    public ParsingContext()
    {
        this(
                new ParseOptions(),
                Optional.empty(),
                Optional.empty());
    }

    public ParseOptions getParseOptions()
    {
        return parseOptions;
    }

    public Optional<Catalog> getCatalog()
    {
        return catalog;
    }

    public Optional<String> getDefaultSchema()
    {
        return defaultSchema;
    }

    private final Cell<SqlParser> parser = Cell.setOnce();

    public Optional<SqlParser> getParser()
    {
        return parser.getOptional();
    }

    public ParsingContext setParser(SqlParser parser)
    {
        this.parser.set(parser);
        return this;
    }

    private final Cell<TNode> originalTreeNode = Cell.setOnce();

    public Optional<TNode> getOriginalTreeNode()
    {
        return originalTreeNode.getOptional();
    }

    public ParsingContext setOriginalTreeNode(TNode originalTreeNode)
    {
        this.originalTreeNode.set(originalTreeNode);
        return this;
    }
}
