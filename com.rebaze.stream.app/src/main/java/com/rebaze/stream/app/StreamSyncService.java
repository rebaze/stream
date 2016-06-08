package com.rebaze.stream.app;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.distribution.DistributionBuilder;
import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.transport.api.TransportAgent;
import com.rebaze.transport.api.TransportMonitor;
import com.rebaze.workspace.api.ResourceLink;

import osgi.enroute.scheduler.api.Scheduler;

@Designate( ocd = StreamSyncService.Config.class )
@Component( scope = ServiceScope.SINGLETON, immediate = true, service = StreamSyncService.class, configurationPolicy = ConfigurationPolicy.OPTIONAL )
public class StreamSyncService implements TransportMonitor
{
    private final Logger LOG = LoggerFactory.getLogger( StreamSyncService.class );
    
    @ObjectClassDefinition( name = "Stream-Tick Configuration" )
    @interface Config
    {
        String tickRemoteSyncPattern() default "*/30 * * * * * *";
    }
    
    @Reference
    IndexAdmin indexAdmin;

    @Reference( target = "(type=composite)" )
    MirrorAdmin mirrorAdmin;
    
    @Reference()
    DistributionBuilder packer;
    
    private transient CompletableFuture<List<ResourceLink>> pipe;

    @Reference
    TransportAgent transportAgent;
    
    @Reference
    private Scheduler scheduler;

    private Closeable schedulerSession;
    

    @Activate
    private void activate( StreamSyncService.Config config, ComponentContext context )
    {
        LOG.info( "#Activating Stream: " + context.getProperties().get( "component.name" ) );
        try
        {
            this.schedulerSession = scheduler.schedule( () -> build(), config.tickRemoteSyncPattern() );
        }
        catch ( Exception e )
        {
           throw new RuntimeException("Scheduler.");
        }

    }
    
   
    @Deactivate
    private void deactivate( ComponentContext context ) throws IOException
    {
        LOG.info( "# Deactivating " + context.getProperties().get( "component.name" ) );
        if (schedulerSession != null) {
            schedulerSession.close();
        }
    }

    public void build()
    {                
        LOG.info( "# Ping: " + pipe );
        LOG.debug( "# Check debug: " + pipe );

        synchronized (this)
        {
            if ( pipe == null || pipe.isDone() )
            {
                LOG.info( "Remote updated started." );
                try
                {
                    pipe = CompletableFuture.supplyAsync( () -> work() );
                    
                } catch ( Exception e )
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private List<ResourceLink> work() {
         List<ResourceDTO> resources = mirrorAdmin.fetchResources();
         List<ResourceLink> fresh = transportAgent.transport( this, resources );
         LOG.info("Complete new artifacts in store: " + fresh.size() );
         if (fresh.size() > 0) {
             packer.pack();
         }
         return fresh;
    }

    @Override
    public void transporting( ResourceDTO resource )
    {
        LOG.info( "Loading " + resource.getUri() );

    }
}
