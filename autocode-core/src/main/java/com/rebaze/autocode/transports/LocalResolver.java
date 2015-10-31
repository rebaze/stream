package com.rebaze.autocode.transports;

import com.rebaze.autocode.config.WorkspaceConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
@Singleton
public class LocalResolver extends AbstractDefaultResolver
{

    @Inject
    public LocalResolver( WorkspaceConfiguration workspaceConfiguration )
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
