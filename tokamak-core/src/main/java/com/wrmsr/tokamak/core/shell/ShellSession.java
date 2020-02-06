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

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class ShellSession
{
    private final TokamakShell shell;

    private Catalog catalog;
    private Optional<String> defaultSchema;

    ShellSession(
            TokamakShell shell,
            Catalog catalog,
            Optional<String> defaultSchema)
    {
        this.shell = checkNotNull(shell);
        this.catalog = checkNotNull(catalog);
        this.defaultSchema = checkNotNull(defaultSchema);
    }

    public TokamakShell getShell()
    {
        return shell;
    }

    public Catalog getCatalog()
    {
        return catalog;
    }

    public Optional<String> getDefaultSchema()
    {
        return defaultSchema;
    }

    public ShellSession setDefaultSchema(Optional<String> defaultSchema)
    {
        this.defaultSchema = checkNotNull(defaultSchema);
        return this;
    }
}
