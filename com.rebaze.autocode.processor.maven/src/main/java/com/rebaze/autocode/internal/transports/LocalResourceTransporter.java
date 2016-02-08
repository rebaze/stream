package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.osgi.service.component.annotations.Component;

import com.rebaze.autocode.config.WorkspaceConfiguration;

/**
 *
 */
@Component
public class LocalResourceTransporter extends AbstractResourceTransporter
{

    public LocalResourceTransporter( WorkspaceConfiguration workspaceConfiguration )
    {
        super( workspaceConfiguration );
    }

    @Override public boolean accept( String protocol )
    {
        return protocol.startsWith( "file" );
    }

    @Override protected File download( URL url ) throws IOException
    {
        try
        {
            return new File(url.toURI());
        }
        catch ( URISyntaxException e )
        {
            throw new IOException( e.getMessage(),e );
        }
    }
}
