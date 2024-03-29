package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.IndexKey;
import com.rebaze.autocode.config.ObjectIndex;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.core.TreeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.rebaze.trees.core.Selector.selector;

/**
 * Can resolve the tree from a given gav
 */
public class StaticGAVResolver implements ResourceResolver<GAV>
{
    public static final Logger LOG = LoggerFactory.getLogger( StaticGAVResolver.class );
    private Map<GAV, Tree> mapToTree = new HashMap<>();

    @Inject
    public StaticGAVResolver( WorkspaceConfiguration config, TreeSession session )
    {
        this(config.getResourceTreeConfiguration(),session);
    }

    public StaticGAVResolver( ResourceTreeConfiguration config, TreeSession session )
    {
        LOG.info( "Indexing tree configuration: " + config );
        for ( ObjectIndex idx : config.getObjects() )
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
            throw new AutocodeException( "Query " + query + " cannot be resolved by " + toString() );
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
