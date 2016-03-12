package com.rebaze.autocode.api.core;

import com.rebaze.trees.core.Tree;

import java.io.File;

/**
 *
 */
public class StagedSubject
{
    private final File file;
    private final AutocodeAddress address;
    private final Tree tree;

    public StagedSubject( AutocodeAddress address, Tree tree, File res )
    {
        this.file = res;
        this.tree = tree;
        this.address = address;
    }

    public File getFile()
    {
        return file;
    }

    public Tree getTree()
    {
        return tree;
    }

    public AutocodeAddress getAddress()
    {
        return address;
    }

    @Override public String toString()
    {
        return "[ StagedSubject file=" + file.getName() + " ]";
    }
}
