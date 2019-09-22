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
package com.wrmsr.tokamak.driver.state;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.plan.node.StatefulNode;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class State
        implements Row
{
    public enum Mode
    {
        STORAGE_CREATED(true),
        STORAGE_SHARED(true),
        STORAGE_EXCLUSIVE(true),

        INVALID(false),
        PHANTOM(false),
        SHARED(false),
        EXCLUSIVE(false),
        MODIFIED(false);

        private final boolean isStorageMode;

        Mode(boolean isStorageMode)
        {
            this.isStorageMode = isStorageMode;
        }

        public boolean isStorageMode()
        {
            return isStorageMode;
        }

        public static Mode fromStorageMode(StorageState.Mode storageMode)
        {
            switch (storageMode) {
                case CREATED:
                    return STORAGE_CREATED;
                case SHARED:
                    return STORAGE_SHARED;
                case EXCLUSIVE:
                    return STORAGE_EXCLUSIVE;
                default:
                    throw new IllegalArgumentException(Objects.toString(storageMode));
            }
        }
    }

    public static class ModeException
            extends RuntimeException
    {
        private static final long serialVersionUID = -7720805908630032266L;

        private final State state;

        public ModeException(State state)
        {
            this.state = checkNotNull(state);
        }

        @Override
        public String toString()
        {
            return "State.ModeException{" +
                    "state=" + state +
                    '}';
        }
    }

    private final StatefulNode node;
    private final Id id;
    private Mode mode;
    private long version;

    @Nullable
    private Object[] attributes;
    private long attributesVersion;

    @Nullable
    private Linkage linkage;
    private long linkageVersion;

    private long updatedFieldsMask;

    @FunctionalInterface
    public interface ModeCallback
    {
        void onMode(State state, Mode oldMode, Mode newMode);
    }

    @Nullable
    private ModeCallback modeCallback;

    public State(StatefulNode node, Id id, Mode mode)
    {
        checkArgument(!mode.isStorageMode);
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
    }

    private State(
            StatefulNode node,
            Id id,
            Mode mode,
            long version,
            @Nullable Object[] attributes,
            long attributesVersion,
            @Nullable Linkage linkage,
            long linkageVersion)
    {
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
        this.version = version;
        this.attributes = attributes;
        this.attributesVersion = attributesVersion;
        this.linkage = linkage;
        this.linkageVersion = linkageVersion;
        checkArgument(mode.isStorageMode);
    }

    public static State newFromStorage(
            StatefulNode node,
            Id id,
            StorageState.Mode storageMode,
            long version,
            @Nullable Object[] attributes,
            long attributesVersion,
            @Nullable Linkage linkage,
            long linkageVersion)
    {
        return new State(
                node,
                id,
                Mode.fromStorageMode(storageMode),
                version,
                attributes,
                attributesVersion,
                linkage,
                linkageVersion);
    }

    @Override
    public String toString()
    {
        return "State{" +
                "node=" + getNode() +
                ", id=" + id +
                ", mode=" + mode +
                '}';
    }

    public StatefulNode getNode()
    {
        return node;
    }

    @Override
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

    @Nullable
    public Object[] getAttributes()
    {
        checkMode(mode != Mode.INVALID && mode != Mode.PHANTOM);
        return attributes;
    }

    public long getAttributesVersion()
    {
        return attributesVersion;
    }

    @Nullable
    public Linkage getLinkage()
    {
        checkMode(mode != Mode.INVALID);
        return linkage;
    }

    public long getLinkageVersion()
    {
        return linkageVersion;
    }

    public boolean isNull()
    {
        checkMode(mode != Mode.INVALID);
        return attributes == null;
    }

    public Map<String, Object> getRowMap()
    {
        checkMode(mode != Mode.INVALID);
        return Collections.unmodifiableMap(new ObjectArrayBackedMap<>(node.getRowLayout().getShape(), attributes));
    }

    public long getUpdatedFieldsMask()
    {
        checkMode(mode != Mode.MODIFIED);
        return updatedFieldsMask;
    }

    public Set<String> getUpdatedFields()
    {
        checkMode(mode != Mode.MODIFIED);
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (int i = 0; i < 64; ++i) {
            if ((updatedFieldsMask & (1L << i)) != 0) {
                builder.add(node.getRowLayout().getFieldNames().get(i));
            }
        }
        return builder.build();
    }

    public State setModeCallback(ModeCallback modeCallback)
    {
        checkState(this.modeCallback == null);
        this.modeCallback = checkNotNull(modeCallback);
        return this;
    }

    public void setInitialMode(Mode mode)
    {
        checkArgument(!mode.isStorageMode);
        checkState(this.mode.isStorageMode);
        this.mode = mode;
        // FIXME: callback?
    }

    public void setAttributes(@Nullable Object[] attributes)
    {
        checkState(!mode.isStorageMode);

        // FIXME: floating point stability, fat comparison, etc
        boolean diff;
        if (attributes != null) {
            checkArgument(attributes.length == getNode().getRowLayout().getFields().size());
            diff = this.attributes == null || Arrays.equals(attributes, this.attributes);
        }
        else {
            diff = this.attributes != null;
        }

        if (!diff && mode != Mode.INVALID) {
            return;
        }

        long updatedFieldsMask;
        if (attributes == null && this.attributes == null) {
            updatedFieldsMask = 0;
        }
        else if (this.attributes != null && attributes != null) {
            updatedFieldsMask = 0;
            for (int i = 0; i < attributes.length; ++i) {
                if (Objects.equals(attributes[i], this.attributes[i])) {
                    updatedFieldsMask |= 1L << i;
                }
            }
        }
        else {
            updatedFieldsMask = (1L << node.getRowLayout().getFieldNames().size()) - 1;
        }

        if (mode != Mode.INVALID && updatedFieldsMask != 0) {
            throw new ModeException(this);
        }

        checkState(this.updatedFieldsMask == 0);
        this.attributes = attributes;
        this.updatedFieldsMask = updatedFieldsMask;
    }

    public void setLinkage(Linkage linkage)
    {
        checkNotNull(linkage);
    }

    private void checkMode()
    {
        if (mode.isStorageMode) {
            throw new ModeException(this);
        }
    }

    private void checkMode(boolean condition)
    {
        if (mode.isStorageMode || !condition) {
            throw new ModeException(this);
        }
    }
}
