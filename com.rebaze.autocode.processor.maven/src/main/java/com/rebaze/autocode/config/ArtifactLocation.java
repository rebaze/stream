package com.rebaze.autocode.config;

import java.util.List;

/**
 * Created by tonit on 28/10/15.
 */
public class ArtifactLocation
{
    private AutocodeAddress checksum;

    private List<URLLocation> locations;

    public AutocodeAddress getChecksum()
    {
        return checksum;
    }

    public void setChecksum( AutocodeAddress checksum )
    {
        this.checksum = checksum;
    }

    public List<URLLocation> getLocations()
    {
        return locations;
    }

    public void setLocations( List<URLLocation> locations )
    {
        this.locations = locations;
    }
}
