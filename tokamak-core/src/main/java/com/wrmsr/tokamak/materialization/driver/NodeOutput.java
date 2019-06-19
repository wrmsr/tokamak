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
package com.wrmsr.tokamak.materialization.driver;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.materialization.api.Payload;
import com.wrmsr.tokamak.materialization.api.Txid;

import javax.annotation.Nullable;

import java.util.List;

public final class NodeOutput
{
    private final Payload payload;
    private final List<Payload> lineage;
    private final Txid txid;

    public NodeOutput(Payload payload, List<Payload> lineage, @Nullable Txid txid)
    {
        this.payload = payload;
        this.lineage = ImmutableList.copyOf(lineage);
        this.txid = txid;
    }

    public Payload getPayload()
    {
        return payload;
    }

    public List<Payload> getLineage()
    {
        return lineage;
    }

    @Nullable
    public Txid getTxid()
    {
        return txid;
    }
}
