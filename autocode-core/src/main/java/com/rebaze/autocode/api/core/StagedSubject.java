package com.rebaze.autocode.api.core;

import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.commons.tree.Tree;

import java.io.File;

/**
 * Created by tonit on 29/10/15.
 */
public class StagedSubject
{
    private final File file;
    private final AutocodeArtifact artifact;
    private final Tree tree;

    public StagedSubject( AutocodeArtifact artifact, Tree tree, File res )
    {
        this.file = res;
        this.tree = tree;
        this.artifact = artifact;
    }

    public File getFile()
    {
        return file;
    }

    public Tree getTree() {
        return tree;
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
