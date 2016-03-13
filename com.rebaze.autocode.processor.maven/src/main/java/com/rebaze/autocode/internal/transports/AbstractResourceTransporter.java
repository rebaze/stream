package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.ops4j.store.Store;
import org.ops4j.store.intern.TemporaryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rebaze.autocode.config.ArtifactLocation;
import com.rebaze.autocode.config.CacheSettings;
import com.rebaze.autocode.config.URLLocation;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.trees.core.Tree;

/**
 * Created by tonit on 30/10/15.
 */
abstract class AbstractResourceTransporter implements ResourceTransporter
{
    private final static Logger LOG = LoggerFactory.getLogger( AbstractResourceTransporter.class );
    private Store<InputStream> store;
    private CacheSettings cache;
    private Multimap<String, String> locations = ArrayListMultimap.create();
     
    public void setConfiguration( WorkspaceConfiguration configuration)
    {
        LOG.info( "Indexing from config {}", configuration );
        this.cache = configuration.getConfiguration().getRepository().getCache();
        store = new TemporaryStore( new File( cache.getFolder() ), false );
        // index:
        for ( ArtifactLocation site : configuration.getSites().getLocations() )
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

    protected Store<InputStream> getStore() {
        return this.store;
    }

    protected abstract File download( URL url ) throws IOException;

}
