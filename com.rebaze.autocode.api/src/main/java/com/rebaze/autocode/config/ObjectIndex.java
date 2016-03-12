package com.rebaze.autocode.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ObjectIndex
{
    private String node;
    private List<IndexKey> index = new ArrayList<>(  );

    public String getNode()
    {
        return node;
    }

    public void setNode( String node )
    {
        this.node = node;
    }

    public List<IndexKey> getIndex()
    {
        return index;
    }

    public void setIndex( List<IndexKey> index )
    {
        this.index = index;
    }
}
