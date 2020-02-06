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
package com.wrmsr.tokamak.core.plan;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.util.Cell;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PlanningContext
{
    private final Optional<Catalog> catalog;
    private final Optional<ParsingContext> parsingContext;

    public PlanningContext(
            Optional<Catalog> catalog,
            Optional<ParsingContext> parsingContext)
    {
        this.catalog = checkNotNull(catalog);
        this.parsingContext = checkNotNull(parsingContext);
    }

    public Optional<Catalog> getCatalog()
    {
        return catalog;
    }

    public Optional<ParsingContext> getParsingContext()
    {
        return parsingContext;
    }

    private final Cell<Plan> originalPlan = Cell.setOnce();

    public Optional<Plan> getOriginalPlan()
    {
        return originalPlan.getOptional();
    }

    public PlanningContext setOriginalPlan(Plan originalPlan)
    {
        this.originalPlan.set(originalPlan);
        return this;
    }
}
