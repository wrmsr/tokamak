package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.materialization.api.NodeName;

import java.util.Map;
import java.util.Optional;

public abstract class StatefulNode
        extends AbstractNode
{
    private final Map<NodeName, Invalidation> invalidations;
    private final Map<NodeName, LinkageMask> linkageMasks;

    public StatefulNode(
            NodeName name,
            Optional<Map<NodeName, Invalidation>> invalidations,
            Optional<Map<NodeName, LinkageMask>> linkageMasks)
    {
        super(name);
        this.invalidations = ImmutableMap.copyOf(invalidations.get());
        this.linkageMasks = ImmutableMap.copyOf(linkageMasks.get());
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
