package com.rebaze.autocode.config;

/**
 * Created by tonit on 27/10/15.
 */
public class Configuration
{
    private String name;

    private String owner;

    private Repository repository;


    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner( String owner )
    {
        this.owner = owner;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    @Override public String toString()
    {
        return "[Configuration name=" + name + "]";
    }
}
