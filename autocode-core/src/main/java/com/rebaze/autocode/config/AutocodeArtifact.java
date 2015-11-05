package com.rebaze.autocode.config;

import java.util.List;

/**
 * Created by tonit on 28/10/15.
 */
public class AutocodeArtifact
{
    private String coordinates;
    private String classifier;
    private String extension;
    private AutocodeAddress address;
    private List<AutocodeAddress> extensions;

    public AutocodeAddress getAddress()
    {
        return address;
    }

    public void setAddress( AutocodeAddress address )
    {
        this.address = address;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension( String extension )
    {
        this.extension = extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates( String coordinates )
    {
        this.coordinates = coordinates;
    }

    public List<AutocodeAddress> getExtensions()
    {
        return extensions;
    }

    public void setExtensions( List<AutocodeAddress> extensions )
    {
        this.extensions = extensions;
    }
}
