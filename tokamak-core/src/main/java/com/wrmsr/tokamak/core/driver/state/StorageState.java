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
package com.wrmsr.tokamak.core.driver.state;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.plan.node.StateNode;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class StorageState
{
    public enum Mode
    {
        CREATED,
        SHARED,
        EXCLUSIVE,
    }

    private final StateNode node;
    private final Id id;
    private final Mode mode;
    private final long version;

    private final float createdAtUtc;
    private final float updatedAtUtc;

    @Nullable
    private final byte[] attributes;
    private long attributesVersion;

    @Nullable
    private final byte[] input;

    @Nullable
    private final byte[] output;

    private long linkageVersion;

    public StorageState(
            StateNode node,
            Id id,
            Mode mode,
            long version,
            float createdAtUtc,
            float updatedAtUtc,
            @Nullable byte[] attributes,
            long attributesVersion,
            @Nullable byte[] input,
            @Nullable byte[] output,
            long linkageVersion)
    {
        checkArgument((input == null) == (output == null));
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
        this.version = version;
        this.createdAtUtc = createdAtUtc;
        this.updatedAtUtc = updatedAtUtc;
        this.attributes = attributes;
        this.attributesVersion = attributesVersion;
        this.input = input;
        this.output = output;
        this.linkageVersion = linkageVersion;
    }

    @Override
    public String toString()
    {
        return "StorageState{" +
                "node=" + node +
                ", id=" + id +
                ", mode=" + mode +
                ", version=" + version +
                '}';
    }

    public StateNode getNode()
    {
        return node;
    }

    public Id getId()
    {
        return id;
    }

    public Mode getMode()
    {
        return mode;
    }

    public long getVersion()
    {
        return version;
    }

    public float getCreatedAtUtc()
    {
        return createdAtUtc;
    }

    public float getUpdatedAtUtc()
    {
        return updatedAtUtc;
    }

    @Nullable
    public byte[] getAttributes()
    {
        return attributes;
    }

    public long getAttributesVersion()
    {
        return attributesVersion;
    }

    @Nullable
    public byte[] getInput()
    {
        return input;
    }

    @Nullable
    public byte[] getOutput()
    {
        return output;
    }

    public long getLinkageVersion()
    {
        return linkageVersion;
    }
}
