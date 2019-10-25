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
package com.wrmsr.tokamak.api;

import static com.wrmsr.tokamak.api.Util.checkState;

public interface Row
{
    // Null id implies an anonymous row (one with no id) which may or may not be an empty row.
    Id getId();

    // Null attributes implies an empty row and an empty rowset. Operations will emit either
    // exactly one Row with null attributes or one-or-more rows all with non-null attributes
    // Empty rows must have a null id. Non-empty rows may or may not have a null id.
    // Attribute object array values may be null.
    Object[] getAttributes();

    default boolean isAnon()
    {
        return getId() != null;
    }

    default boolean isEmpty()
    {
        if(getAttributes() == null) {
            checkState(isAnon());
            return true;
        }
        else {
            return false;
        }
    }
}
