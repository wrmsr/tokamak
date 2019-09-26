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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.core.plan.node.StatefulNode;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class State
        implements Row
{
    public enum Mode
    {
        CONSTRUCTING,

        INVALID,
        PHANTOM,
        SHARED,
        EXCLUSIVE,
        MODIFIED;
    }

    public static class ModeException
            extends RuntimeException
    {
        private static final long serialVersionUID = -7720805908630032266L;

        private final State state;
        private final Optional<Mode> newMode;

        public ModeException(State state, Optional<Mode> newMode)
        {
            this.state = checkNotNull(state);
            this.newMode = checkNotNull(newMode);
        }

        public ModeException(State state, Mode newMode)
        {
            this(state, Optional.of(newMode));
        }

        public ModeException(State state)
        {
            this(state, Optional.empty());
        }

        public State getState()
        {
            return state;
        }

        public Optional<Mode> getNewMode()
        {
            return newMode;
        }

        @Override
        public String toString()
        {
            return "ModeException{" +
                    "state=" + state +
                    ", newMode=" + newMode +
                    '}';
        }
    }

    private final StatefulNode node;
    private final Id id;
    private Mode mode;
    private long version;

    @Nullable
    private final StorageState.Mode storageMode;

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
        void onMode(State state, Mode oldMode);
    }

    @Nullable
    private ModeCallback modeCallback;

    public State(StatefulNode node, Id id, Mode mode)
    {
        checkArgument(mode != Mode.CONSTRUCTING);
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
        this.storageMode = null;
    }

    private State(
            StatefulNode node,
            Id id,
            long version,
            @Nullable StorageState.Mode storageMode,
            @Nullable Object[] attributes,
            long attributesVersion,
            @Nullable Linkage linkage,
            long linkageVersion)
    {
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = Mode.CONSTRUCTING;
        this.version = version;
        this.storageMode = storageMode;
        this.attributes = attributes;
        this.attributesVersion = attributesVersion;
        this.linkage = linkage;
        this.linkageVersion = linkageVersion;
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
                version,
                storageMode,
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
    public StorageState.Mode getStorageMode()
    {
        return storageMode;
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
        checkNotNull(attributes);
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
        checkState(this.mode == Mode.CONSTRUCTING);
        checkArgument(mode != Mode.CONSTRUCTING);
        this.mode = mode;
    }

    public void setAttributes(@Nullable Object[] attributes)
    {
        checkState(mode != Mode.CONSTRUCTING);

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
        updateMode((diff || storageMode == StorageState.Mode.CREATED) ? Mode.MODIFIED : Mode.EXCLUSIVE);
    }

    public static boolean isValidStateTransition(Mode from, Mode to)
    {
        checkNotNull(from);
        checkNotNull(to);
        switch (from) {
            case CONSTRUCTING:
                return false;
            case INVALID:
                return to == Mode.EXCLUSIVE || to == Mode.MODIFIED;
            case PHANTOM:
                return to == Mode.EXCLUSIVE;
            case SHARED:
            case EXCLUSIVE:
            case MODIFIED:
                return false;
            default:
                throw new IllegalStateException(from.toString());
        }
    }

    private void updateMode(Mode newMode)
    {
        if (mode == newMode) {
            return;
        }
        if (!isValidStateTransition(mode, newMode)) {
            throw new ModeException(this, newMode);
        }

        Mode oldMode = mode;
        mode = newMode;

        if (modeCallback != null) {
            modeCallback.onMode(this, oldMode);
        }
    }

    public void setLinkage(Linkage linkage)
    {
        checkNotNull(linkage);
        checkMode(mode == Mode.EXCLUSIVE || mode == Mode.MODIFIED);
        this.linkage = linkage;
    }

    private void checkMode()
    {
        if (mode == Mode.CONSTRUCTING) {
            throw new ModeException(this);
        }
    }

    private void checkMode(boolean condition)
    {
        if (mode == Mode.CONSTRUCTING) {
            throw new ModeException(this);
        }
    }
}
