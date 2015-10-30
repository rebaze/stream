package com.rebaze.autocode.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonit on 28/10/15.
 */
public class ArtifactLookupSites
{
    private List<ArtifactLocation> locations = new ArrayList<>(  );

    public List<ArtifactLocation> getLocations()
    {
        return locations;
    }

    public void setLocations( List<ArtifactLocation> locations )
    {
        this.locations = locations;
    }

    @Override public String toString()
    {
        return "[ArtifactLookupSites locations=" + locations.size() + " ]";
    }
}
