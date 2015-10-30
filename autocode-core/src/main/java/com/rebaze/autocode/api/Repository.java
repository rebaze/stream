package com.rebaze.autocode.api;

import java.util.List;

/**
 * Created by tonit on 27/10/15.
 */
public class Repository
{
    private CacheSettings cache;

    private List<BuildSubject> subjects;

    public Repository()
    {
    }

    public CacheSettings getCache()
    {
        return cache;
    }

    public void setCache( CacheSettings cache )
    {
        this.cache = cache;
    }

    public List<BuildSubject> getSubjects()
    {
        return subjects;
    }

    public void setSubjects( List<BuildSubject> subjects )
    {
        this.subjects = subjects;
    }
}
