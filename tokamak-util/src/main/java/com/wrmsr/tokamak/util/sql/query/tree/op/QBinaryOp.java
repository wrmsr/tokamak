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
package com.wrmsr.tokamak.util.sql.query.tree.op;

import java.util.Optional;

public class QBinaryOp
        extends QOp
{
    public QBinaryOp(Optional<String> ansi, boolean requiresSpace)
    {
        super(2, ansi, requiresSpace);
    }

    public QBinaryOp(String ansi)
    {
        this(Optional.of(ansi), false);
    }
}
