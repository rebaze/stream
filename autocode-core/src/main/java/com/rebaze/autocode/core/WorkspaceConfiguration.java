package com.rebaze.autocode.core;

import com.rebaze.autocode.api.ArtifactLookupSites;
import com.rebaze.autocode.api.Configuration;

/**
 * Created by tonit on 29/10/15.
 */
public interface WorkspaceConfiguration
{
    public Configuration getConfiguration();

    public ArtifactLookupSites getSites();

}
