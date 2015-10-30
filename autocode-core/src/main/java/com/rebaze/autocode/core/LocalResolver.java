package com.rebaze.autocode.core;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
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
