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
 *
 */
package com.wrmsr.tokamak.driver;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Payload;
import com.wrmsr.tokamak.api.Txid;

import javax.annotation.concurrent.Immutable;

import java.util.List;

@Immutable
public final class NodeOutput
{
    private final Payload payload;
    private final List<Payload> lineage;

    public NodeOutput(Payload payload, List<Payload> lineage)
    {
        this.payload = payload;
        this.lineage = ImmutableList.copyOf(lineage);
    }

    public Payload getPayload()
    {
        return payload;
    }

    public List<Payload> getLineage()
    {
        return lineage;
    }
}
