package com.rebaze.autocode.config;

import java.util.List;

/**
 * Created by tonit on 27/10/15.
 */
public class BuildSubject
{
    private String type;
    private String vendor;
    private SourceSettings source;
    private List<SubjectVersion> distributions;

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor( String vendor )
    {
        this.vendor = vendor;
    }

    public SourceSettings getSource()
    {
        return source;
    }

    public void setSource( SourceSettings source )
    {
        this.source = source;
    }

    public List<SubjectVersion> getDistributions()
    {
        return distributions;
    }

    public void setDistributions( List<SubjectVersion> distributions )
    {
        this.distributions = distributions;
    }
}
