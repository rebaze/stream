package com.rebaze.autocode.internal.transports;

import static com.rebaze.trees.core.Selector.selector;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.IndexKey;
import com.rebaze.autocode.config.ObjectIndex;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.core.TreeSession;

/**
 * Can resolve the tree from a given gav
 */
@Component(immediate=true)
public class StaticGAVResolver implements ResourceResolver<GAV>
{
    public static final Logger LOG = LoggerFactory.getLogger( StaticGAVResolver.class );
    private Map<GAV, Tree> mapToTree = new HashMap<>();
    
    @Reference
    WorkspaceConfiguration config;
    
    @Reference
    TreeSession session;    

    @Activate
    public void index()
    {
        LOG.info( "Indexing tree configuration: " + config );
        for ( ObjectIndex idx : config.getResourceTreeConfiguration().getObjects() )
        {
            Tree tree = session.createTree( selector( "default" ), idx.getNode() );
            for ( IndexKey key : idx.getIndex() )
            {
                if ( "gav".equalsIgnoreCase( key.getType() ) )
                {
                    mapToTree.put( GAV.fromString( key.getParameter() ), tree );
                }
            }
        }
    }

    @Override public Tree resolve( GAV query ) throws AutocodeException
    {
        // Will lookup gav from config file:
        Tree result = mapToTree.get( query );
        if ( result == null )
        {
            //LOG.info( "Query " + query + " cannot be resolved by " + toString() );
        }
        return result;
    }

    @Override public String toString()
    {
        return "StaticGAVResolver{" +
            "mapToTree=" + mapToTree.size() +
            '}';
    }
}
