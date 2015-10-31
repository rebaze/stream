package com.rebaze.autocode.config;

/**
 * Created by tonit on 28/10/15.
 */
public class AutocodeArtifact
{
    private String coordinates;
    private String classifier;
    private String extension;
    private AutocodeChecksum checksum;

    public AutocodeChecksum getChecksum()
    {
        return checksum;
    }

    public void setChecksum( AutocodeChecksum checksum )
    {
        this.checksum = checksum;
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
}
