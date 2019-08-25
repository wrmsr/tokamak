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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.node.StatefulNode;

import javax.annotation.Nullable;

import java.util.BitSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class State
        implements Row
{
    public enum Mode
    {
        NOT_SET,
        INVALID,
        PHANTOM,
        SHARED,
        EXCLUSIVE,
        MODIFIED
    }

    public static class ModeException
            extends RuntimeException
    {
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

    @Nullable
    private Linkage linkage;

    @Nullable
    private BitSet updatedFieldBitSet;

    @FunctionalInterface
    public interface ModeCallback
    {
        void onMode(State state, Mode oldMode, Mode newMode);
    }

    @Nullable
    private ModeCallback modeCallback;

    public State(StatefulNode node, Id id, Mode mode)
    {
        checkArgument(mode != Mode.NOT_SET);
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.mode = checkNotNull(mode);
    }

    private State(
            StatefulNode node,
            Id id,
            long version,
            @Nullable Object[] attributes,
            @Nullable Linkage linkage)
    {
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.version = version;
        this.attributes = attributes;
        this.linkage = linkage;
        mode = Mode.NOT_SET;
    }

    public static State newFromStorage(
            StatefulNode node,
            Id id,
            long version,
            @Nullable Object[] attributes,
            @Nullable Linkage linkage)
    {
        return new State(
                node,
                id,
                version,
                attributes,
                linkage);
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

    @Nullable
    public Linkage getLinkage()
    {
        checkMode(mode != Mode.INVALID);
        return linkage;
    }

    public BitSet getUpdatedFieldBitSet()
    {
        checkMode(mode != Mode.MODIFIED);
        return checkNotNull(updatedFieldBitSet);
    }

    public Set<String> getUpdatedFields()
    {
        return getUpdatedFieldBitSet().stream().mapToObj(getNode().getRowLayout().getFields()::get).collect(toImmutableSet());
    }

    public State setModeCallback(ModeCallback modeCallback)
    {
        checkState(this.modeCallback == null);
        this.modeCallback = checkNotNull(modeCallback);
        return this;
    }

    public void setAttributes(@Nullable Object[] attributes)
    {
        checkMode(mode == Mode.INVALID);
        if (attributes != null) {
            checkArgument(attributes.length == getNode().getRowLayout().getFields().size());
        }
        // if (this.row != null)
    }

    public void setLinkage(Linkage linkage)
    {
        checkNotNull(linkage);
    }

    private void checkMode()
    {
        if (mode == Mode.NOT_SET) {
            throw new ModeException(this);
        }
    }

    private void checkMode(boolean condition)
    {
        if (mode == Mode.NOT_SET || !condition) {
            throw new ModeException(this);
        }
    }
}
