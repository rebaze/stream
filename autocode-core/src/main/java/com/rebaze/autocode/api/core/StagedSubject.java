package com.rebaze.autocode.api.core;

import com.rebaze.autocode.config.AutocodeArtifact;

import java.io.File;

/**
 * Created by tonit on 29/10/15.
 */
public class StagedSubject
{
    private final File file;
    private final AutocodeArtifact artifact;

    public StagedSubject( AutocodeArtifact artifact, File res )
    {
        this.file = res;
        this.artifact = artifact;
    }

    public File getFile()
    {
        return file;
    }

    public AutocodeArtifact getArtifact()
    {
        return artifact;
    }

    @Override public String toString()
    {
        return "[ StagedSubject file=" + file.getName() + " ]";
    }
}
