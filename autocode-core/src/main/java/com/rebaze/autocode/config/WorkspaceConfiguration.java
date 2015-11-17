package com.rebaze.autocode.config;

import com.rebaze.autocode.config.ArtifactLookupSites;
import com.rebaze.autocode.config.Configuration;

/**
 * Created by tonit on 29/10/15.
 */
public interface WorkspaceConfiguration
{
    public Configuration getConfiguration();

    public ArtifactLookupSites getSites();

    ResourceTreeConfiguration getResourceTreeConfiguration();
}
