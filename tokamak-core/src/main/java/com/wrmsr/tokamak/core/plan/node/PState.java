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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PState
        extends PAbstractNode
        implements PSingleSource
{
    public enum Denormalization
    {
        NONE,
        INPUT,
    }

    @Immutable
    public static final class Invalidation
    {
        private final String field;
        private final boolean soft;

        @JsonCreator
        public Invalidation(
                @JsonProperty("field") String field,
                @JsonProperty("soft") boolean soft)
        {
            this.field = checkNotEmpty(field);
            this.soft = soft;
        }

        @Override
        public String toString()
        {
            return "Invalidation{" +
                    "field=" + field +
                    ", soft=" + soft +
                    '}';
        }

        @JsonProperty("fields")
        public String getField()
        {
            return field;
        }

        @JsonProperty("soft")
        public boolean isSoft()
        {
            return soft;
        }
    }

    @Immutable
    public static final class LinkageMask
    {
        private final Set<String> fields;

        @JsonCreator
        public LinkageMask(
                @JsonProperty("fields") Set<String> fields)
        {
            this.fields = checkNotEmpty(ImmutableSet.copyOf(fields));
        }

        @Override
        public String toString()
        {
            return "LinkageMask{" +
                    "fields=" + fields +
                    '}';
        }

        @JsonProperty("fields")
        public Set<String> getFields()
        {
            return fields;
        }
    }

    @Immutable
    public static final class LockOverride
    {
        private final String node;
        private final String field;  // FIXME: ordered set
        private final boolean spill;

        @JsonCreator
        public LockOverride(
                @JsonProperty("node") String node,
                @JsonProperty("field") String field,
                @JsonProperty("spill") boolean spill)
        {
            this.node = checkNotNull(node);
            this.field = checkNotNull(field);
            this.spill = spill;
        }

        @Override
        public String toString()
        {
            return "LockOverride{" +
                    "node='" + node + '\'' +
                    ", field='" + field + '\'' +
                    ", spill=" + field + +
                    '}';
        }

        @JsonProperty("node")
        public String getNode()
        {
            return node;
        }

        @JsonProperty("field")
        public String getField()
        {
            return field;
        }

        @JsonProperty("spill")
        public boolean isSpill()
        {
            return spill;
        }
    }

    private final PNode source;
    private final List<PWriterTarget> writerTargets;
    private final Denormalization denormalization;
    private final Map<String, Invalidation> invalidations;
    private final Map<String, LinkageMask> linkageMasks;
    private final Optional<LockOverride> lockOverride;

    @JsonCreator
    public PState(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("writerTargets") List<PWriterTarget> writerTargets,
            @JsonProperty("denormalization") Denormalization denormalization,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.writerTargets = ImmutableList.copyOf(writerTargets);
        this.denormalization = checkNotNull(denormalization);
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
        this.lockOverride = checkNotNull(lockOverride);

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("writerTargets")
    public List<PWriterTarget> getWriterTargets()
    {
        return writerTargets;
    }

    @JsonProperty("denormalization")
    public Denormalization getDenormalization()
    {
        return denormalization;
    }

    @JsonProperty("invalidations")
    public Map<String, Invalidation> getInvalidations()
    {
        return invalidations;
    }

    @JsonProperty("linkageMasks")
    public Map<String, LinkageMask> getLinkageMasks()
    {
        return linkageMasks;
    }

    @JsonProperty("lockOverride")
    public Optional<LockOverride> getLockOverride()
    {
        return lockOverride;
    }

    @Override
    public FieldCollection getFields()
    {
        return source.getFields();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitState(this, context);
    }
}
