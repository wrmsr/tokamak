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

public final class PlanValidation
{
    /*
    TODO:
     - node name refs (invals, etc)
     - field name refs (linkagemasks, etc)
     - scope containment
     - cwtc.plan.validate?
      - nothing requiring a full blown ana? or are these just simple anas?

    TODO NOT:
     - simple types (done in node ctors)
     - anns (doen in node ctors)
    */

    private PlanValidation()
    {
    }

    public static void validatePlan(Plan plan)
    {
    }
}
