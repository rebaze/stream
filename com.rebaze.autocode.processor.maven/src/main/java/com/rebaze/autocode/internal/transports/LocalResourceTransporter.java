package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.autocode.config.WorkspaceConfiguration;

/**
 *
 */
@Component(service=ResourceTransporter.class)
public class LocalResourceTransporter extends AbstractResourceTransporter
{
	@Reference(bind="setConfiguration") 
	WorkspaceConfiguration configuration;

    @Override public boolean accept( String protocol )
    {
        return protocol.startsWith( "file" );
    }

    @Override protected File download( URL url ) throws IOException
    {
        try{
            return new File(url.toURI());
        }
        catch ( URISyntaxException e )
        {
            throw new IOException( e.getMessage(),e );
        }
    }
}
