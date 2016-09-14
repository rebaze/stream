package com.rebaze.stream.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;

import okio.Okio;

@Designate( ocd = AutoConfig.Config.class )
@Component( immediate = true, name = "Stream Autoconfig", configurationPid = "com.rebaze.stream.configuration" )
public class AutoConfig
{

    private static final Logger LOG = LoggerFactory.getLogger( AutoConfig.class );

    private ServiceRegistration<StreamDefinitionDTO> reg;

    private List<ServiceRegistration<StreamSourceDTO>> streams = new ArrayList<>();

    private Closeable schedulerSession;

    @ObjectClassDefinition( name = "Stream Configuration" )
    @interface Config
    {
        String streamLocation() default "stream.json";

        String storagePath() default "target/repo";
    }

    @Activate
    public void config( BundleContext ctx, Config config ) throws Exception
    {
        // See if we find a appropriate file to load:
        LOG.info( "Configuration: " + config );

        try (InputStream stream = openStream( config.streamLocation() ))
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            StreamDefinitionDTO definition = gson.fromJson(
                    new InputStreamReader( Okio.buffer( Okio.source( stream ) ).inputStream() ),
                    StreamDefinitionDTO.class );
            definition.localPath = new File( config.storagePath() ).getAbsolutePath();
            reg = ctx.registerService( StreamDefinitionDTO.class, definition, null );
            LOG.info( "Configuration from " + config.streamLocation() + ". " + "(" + reg.toString()
                    + ") successfully created: " + definition );
            // expose Whiteboards for every source:
            for ( StreamSourceDTO src : definition.sources )
            {
                System.out.println( "Registered Source: " + src.name + "" );

                // copy over the metadata:
                Dictionary<String, String> dict = new Hashtable<>();
                dict.put( "name", src.name );
                dict.put( "type", src.type );
                dict.put( "active", new Boolean(src.active).toString() );
                if (src.filter != null) {
                    dict.put( "filter", src.filter );
                }
                streams.add( ctx.registerService( StreamSourceDTO.class, src, dict ) );
            }
            // then schedule the sync service with the scheduler.

        }
    }

    private InputStream openStream( String streamLocation ) throws Exception
    {
        if ( streamLocation.contains( "//" ) )
        {
            return new URI( streamLocation ).toURL().openStream();
        }
        else
        {
            return new FileInputStream( new File( streamLocation ) );
        }
    }

    @Deactivate
    public void deactivate( BundleContext ctx ) throws IOException
    {
        LOG.info( "Autoconfig down." );
        streams.forEach( s -> s.unregister() );
        if ( schedulerSession != null )
        {
            schedulerSession.close();
        }
        if ( reg != null )
        {
            reg.unregister();
        }
    }

}
