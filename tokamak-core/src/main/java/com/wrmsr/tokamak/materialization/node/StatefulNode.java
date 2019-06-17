package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.materialization.api.NodeName;

import java.util.Map;

public abstract class StatefulNode
        extends Node
{
    private final Map<NodeName, Invalidation> invalidations;
    private final Map<NodeName, LinkageMask> linkageMasks;

    public StatefulNode(
            Map<NodeName, Invalidation> invalidations,
            Map<NodeName, LinkageMask> linkageMasks)
    {
        super();
        this.invalidations = ImmutableMap.copyOf(invalidations);
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks);
    }

    public Map<NodeName, Invalidation> getInvalidations()
    {
        return invalidations;
    }

    public Map<NodeName, LinkageMask> getLinkageMasks()
    {
        return linkageMasks;
    }

    public boolean isInputDenromalized()
    {
        return false;
    }
}
