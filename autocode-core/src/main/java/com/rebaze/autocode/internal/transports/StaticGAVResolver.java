package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.core.GAV;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.commons.tree.Tree;

import javax.inject.Inject;

/**
 * Can resolve the tree from a given gav
 */
public class StaticGAVResolver implements ResourceResolver<GAV>
{
    private final ResourceTreeConfiguration config;

    @Inject
    public StaticGAVResolver(ResourceTreeConfiguration config) {
        this.config = config;
        // index for gav:
        

    }

    @Override public Tree resolve( GAV query )
    {
        // Will lookup gav from config file:
        
        return null;
    }
}
