package com.wrmsr.tokamak.node;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class StatefulNode
        extends AbstractNode
{
    private final Map<String, Invalidation> invalidations;
    private final Map<String, LinkageMask> linkageMasks;

    public StatefulNode(
            String name,
            Map<String, Invalidation> invalidations,
            Map<String, LinkageMask> linkageMasks)
    {
        super(name);
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
    }

    public Map<String, Invalidation> getInvalidations()
    {
        return invalidations;
    }

    public Map<String, LinkageMask> getLinkageMasks()
    {
        return linkageMasks;
    }

    public boolean isInputDenormalized()
    {
        return false;
    }
}
