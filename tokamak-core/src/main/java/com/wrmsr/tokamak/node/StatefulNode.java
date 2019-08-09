package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class StatefulNode
        extends AbstractNode
{
    private final Map<String, Invalidation> invalidations;
    private final Map<String, LinkageMask> linkageMasks;
    private final Optional<LockOverride> lockOverride;

    protected StatefulNode(
            String name,
            Map<String, Invalidation> invalidations,
            Map<String, LinkageMask> linkageMasks,
            Optional<LockOverride> lockOverride)
    {
        super(name);
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
        this.lockOverride = checkNotNull(lockOverride);
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

    public boolean isInputDenormalized()
    {
        return false;
    }
}
