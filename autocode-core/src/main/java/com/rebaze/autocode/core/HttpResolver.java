package com.rebaze.autocode.core;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import org.ops4j.store.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class HttpResolver extends AbstractDefaultResolver
{
    private final static Logger LOG = LoggerFactory.getLogger( HttpResolver.class );
    private OkHttpClient client = new OkHttpClient();

    @Inject
    public HttpResolver( WorkspaceConfiguration workspaceConfiguration )
    {
        super(workspaceConfiguration);
    }

    protected File download( URL url ) throws IOException
    {
        Request request = new Request.Builder()
            .url( url )
            .build();
        Response response = client.newCall( request ).execute();
        ResponseBody body = response.body();
        LOG.info( "Loading " + body.contentLength() + " bytes of type " + body.contentType() + ".." );
        Handle handle = getStore().store( body.byteStream() );
        LOG.info( "DONE: " + handle.getIdentification() );
        return new File( getStore().getLocation( handle ).getPath() );
    }

    @Override public boolean accept( String protocol )
    {
        return protocol.startsWith( "http" );
    }
}
