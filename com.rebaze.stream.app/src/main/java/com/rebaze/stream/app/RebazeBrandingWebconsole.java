package com.rebaze.stream.app;

import org.apache.felix.webconsole.BrandingPlugin;
import org.osgi.service.component.annotations.Component;

@Component
public class RebazeBrandingWebconsole implements BrandingPlugin
{

    public static final String PREFIX = "/res";
    
    @Override
    public String getBrandName()
    {
        return "Rebaze Stream Webconsole";
    }

    @Override
    public String getFavIcon()
    {
        
        return PREFIX + "/imgs/favicon.ico";
    }

    @Override
    public String getMainStyleSheet()
    {
        return PREFIX + "/ui/webconsole.css";
    }

    @Override
    public String getProductImage()
    {
        return PREFIX + "/imgs/logo.png";
    }

    @Override
    public String getProductName()
    {
        return "Rebaze Stream";
    }

    @Override
    public String getProductURL()
    {
        return "http://stream.rebaze.io";
    }

    @Override
    public String getVendorImage()
    {
        return PREFIX + "/imgs/logo.png";
    }

    @Override
    public String getVendorName()
    {
        return "rebaze GmbH";
    }

    @Override
    public String getVendorURL()
    {
        return "http://www.rebaze.com";
    }

}
