package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.core.GAV;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.IndexKey;
import com.rebaze.autocode.config.ObjectIndex;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.commons.tree.Tree;
import com.rebaze.commons.tree.util.TreeSession;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.rebaze.commons.tree.Selector.selector;

/**
 * Can resolve the tree from a given gav
 */
public class StaticGAVResolver implements ResourceResolver<GAV>
{
    private final ResourceTreeConfiguration config;
    Map<GAV,Tree> mapToTree = new HashMap<>();

    @Inject
    public StaticGAVResolver(ResourceTreeConfiguration config, TreeSession session) {
        this.config = config;
        for ( ObjectIndex idx : config.getObjects()) {
            Tree tree = session.tree(selector(""),idx.getNode());
            for ( IndexKey key : idx.getIndex()) {
                if ("gav".equalsIgnoreCase( key.getType() )) {
                    mapToTree.put( GAV.fromString( key.getParameter() ),tree );
                }
            }
        }
    }

    @Override public Tree resolve( GAV query )
    {
        // Will lookup gav from config file:
        Tree result = mapToTree.get(query);
        if (result == null) {
            throw new AutocodeException( "Query " + query + " cannot be resolved by " + StaticGAVResolver.class.getSimpleName());
        }
        return result;
    }
}
