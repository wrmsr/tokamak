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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PState
        extends PAbstractNode
        implements PInvalidatable, PInvalidator, PSingleSource
{
    public enum Denormalization
    {
        NONE,
        INPUT,
    }

    @Immutable
    public static final class LockOverride
    {
        public enum Spilling
        {
            NONE,
            SPILL,
        }

        private final String node;
        private final String field;  // FIXME: ordered set
        private final Spilling spilling;

        @JsonCreator
        public LockOverride(
                @JsonProperty("node") String node,
                @JsonProperty("field") String field,
                @JsonProperty("spilling") Spilling spilling)
        {
            this.node = checkNotNull(node);
            this.field = checkNotNull(field);
            this.spilling = checkNotNull(spilling);
        }

        @Override
        public String toString()
        {
            return "LockOverride{" +
                    "node='" + node + '\'' +
                    ", field='" + field + '\'' +
                    ", spilling=" + spilling + +
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

        @JsonProperty("spilling")
        public Spilling getSpilling()
        {
            return spilling;
        }
    }

    private final PNode source;
    private final Denormalization denormalization;
    private final List<PInvalidation> invalidations;
    private final Optional<LockOverride> lockOverride;

    private FieldCollection fields;

    @JsonCreator
    public PState(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("denormalization") Denormalization denormalization,
            @JsonProperty("invalidations") List<PInvalidation> invalidations,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.denormalization = checkNotNull(denormalization);
        this.invalidations = ImmutableList.copyOf(invalidations);
        this.lockOverride = checkNotNull(lockOverride);

        fields = source.getFields()
                .withOnlyTransitiveAnnotations()
                .withAnnotations(annotations.getFields());

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("denormalization")
    public Denormalization getDenormalization()
    {
        return denormalization;
    }

    @JsonProperty("invalidations")
    public List<PInvalidation> getInvalidations()
    {
        return invalidations;
    }

    @JsonProperty("lockOverride")
    public Optional<LockOverride> getLockOverride()
    {
        return lockOverride;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitState(this, context);
    }
}
