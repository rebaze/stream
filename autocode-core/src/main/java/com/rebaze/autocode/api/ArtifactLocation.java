package com.rebaze.autocode.api;

import java.util.List;

/**
 * Created by tonit on 28/10/15.
 */
public class ArtifactLocation
{
    private AutocodeChecksum checksum;

    private List<URLLocation> locations;

    public AutocodeChecksum getChecksum()
    {
        return checksum;
    }

    public void setChecksum( AutocodeChecksum checksum )
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
