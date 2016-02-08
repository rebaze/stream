package com.rebaze.autocode.config;

/**
 * Created by tonit on 28/10/15.
 */
public class AutocodeAddress
{
    private String type;
    private String data;

    public AutocodeAddress(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public AutocodeAddress() {

    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getData()
    {
        return data;
    }

    public void setData( String data )
    {
        this.data = data;
    }
}
