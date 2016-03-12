package com.rebaze.autocode.config;

/**
 * Created by tonit on 29/10/15.
 */
public interface WorkspaceConfiguration
{
    Configuration getConfiguration();

    ArtifactLookupSites getSites();

    ResourceTreeConfiguration getResourceTreeConfiguration();
}
