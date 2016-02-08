package com.rebaze.autocode.internal.transports;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rebaze.autocode.config.*;
import com.rebaze.trees.core.Tree;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Created by tonit on 30/10/15.
 */
abstract class AbstractResourceTransporter implements ResourceTransporter
{
    private final static Logger LOG = LoggerFactory.getLogger( AbstractResourceTransporter.class );
    private final Store<InputStream> store;
    private final CacheSettings cache;
    private Multimap<String, String> locations = ArrayListMultimap.create();

    public AbstractResourceTransporter( WorkspaceConfiguration workspaceConfiguration )
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
    public File transport( Tree tree )
    {
        // check if we already have it:
        Collection<String> urls = locations.get( tree.fingerprint() );
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

            return res;
        }else {
            return null;
        }
    }

    protected Store getStore() {
        return this.store;
    }

    protected abstract File download( URL url ) throws IOException;

}
