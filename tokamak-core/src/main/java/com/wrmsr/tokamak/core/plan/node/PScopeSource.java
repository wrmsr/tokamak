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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;
import javax.crypto.spec.PSource;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PScopeSource
        extends PAbstractNode
        implements PSource
{
    private final String sourceName;

    @JsonCreator
    public PScopeSource(
            @JsonProperty("name") String name,
            PNodeAnnotations annotations,
            String sourceName)
    {
        super(name, annotations);

        this.sourceName = checkNotEmpty(sourceName);

        checkInvariants();
    }
}
