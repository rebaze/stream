package com.rebaze.autocode.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonit on 16/11/15.
 */
public class ResourceTreeConfiguration
{
    private List<ObjectIndex> objects = new ArrayList<>(  );

    public List<ObjectIndex> getObjects()
    {
        return objects;
    }

    public void setObjects( List<ObjectIndex> objects )
    {
        this.objects = objects;
    }

    @Override public String toString()
    {
        return "[ResourceTreeConfiguration objects=" + objects.size() + " ]";
    }
}
