package com.rebaze.autocode.internal.transports;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rebaze.autocode.api.core.AutocodeArtifactResolver;
import com.rebaze.autocode.api.core.StagedSubject;
import com.rebaze.autocode.config.*;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Created by tonit on 30/10/15.
 */
public abstract class  AbstractDefaultResolver implements AutocodeArtifactResolver
{
    private final static Logger LOG = LoggerFactory.getLogger( AbstractDefaultResolver.class );
    private final Store<InputStream> store;
    private final CacheSettings cache;
    private Multimap<String, String> locations = ArrayListMultimap.create();

    @Inject
    public AbstractDefaultResolver( WorkspaceConfiguration workspaceConfiguration )
    {

        LOG.info( "Indexing from config {}", workspaceConfiguration );
        this.cache = workspaceConfiguration.getConfiguration().getRepository().getCache();
        store = StoreFactory.newStore( new File( cache.getFolder() ), false );
        // index:
        for ( ArtifactLocation site : workspaceConfiguration.getSites().getLocations() )
        {
            for ( URLLocation location : site.getLocations() )
            {
                locations.put( site.getChecksum().getData(), location.getUrl() );
            }
        }
    }

    @Override
    public StagedSubject download( AutocodeArtifact artifact ) throws IOException
    {
        // check if we already have it:


        Collection<String> urls = locations.get( artifact.getAddress().getData() );
        File res = null;
        if ( urls != null && urls.size() > 0 )
        {
            for ( String url : urls )
            {
                try {
                    URL realURL = new URL(url);
                    if (accept(realURL.getProtocol()))
                    {
                        // handle:
                        res = download( realURL );
                        // register the new artifact and its location:

                        if ( res != null )
                        {
                            break;
                        }
                    }
                }catch (Exception e) {
                    // not handeled.
                    LOG.warn(e.getMessage());
                }

            }
        }
        if ( res != null )
        {

            return new StagedSubject(artifact,res);
        }else {
            return null;
        }
    }

    protected Store getStore() {
        return this.store;
    }

    protected abstract File download( URL url ) throws IOException;

}