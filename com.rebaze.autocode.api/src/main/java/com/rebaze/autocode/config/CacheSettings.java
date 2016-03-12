package com.rebaze.autocode.config;

/**
 *
 */
public class CacheSettings
{
    private String folder;

    public String getFolder()
    {
        return folder;
    }

    public void setFolder( String folder )
    {
        this.folder = folder;
    }

    @Override public String toString()
    {
        return "[CacheSettings folder=" + folder + " ]";
    }
}
