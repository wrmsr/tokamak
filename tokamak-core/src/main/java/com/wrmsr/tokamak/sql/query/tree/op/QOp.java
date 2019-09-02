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
package com.wrmsr.tokamak.sql.query.tree.op;

import com.wrmsr.tokamak.sql.query.tree.QTree;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class QOp
        extends QTree
{
    private final int arity;
    private final Optional<String> ansi;
    private final boolean requiresSpace;

    public QOp(int arity, Optional<String> ansi, boolean requiresSpace)
    {
        checkArgument(arity > 0);
        this.arity = arity;
        this.ansi = checkNotNull(ansi);
        this.requiresSpace = requiresSpace;
    }

    public int getArity()
    {
        return arity;
    }

    public Optional<String> getAnsi()
    {
        return ansi;
    }

    public boolean isRequiresSpace()
    {
        return requiresSpace;
    }
}
