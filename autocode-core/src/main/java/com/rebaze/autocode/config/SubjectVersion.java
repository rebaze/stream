package com.rebaze.autocode.config;

import java.util.List;

/**
 * Created by tonit on 28/10/15.
 */
public class SubjectVersion
{
    private String version;
    private String scmTag;
    private List<AutocodeArtifact> artifacts;

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public List<AutocodeArtifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts( List<AutocodeArtifact> artifacts )
    {
        this.artifacts = artifacts;
    }
}
