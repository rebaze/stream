package com.rebaze.autocode.internal.fs;

import com.rebaze.tree.api.StreamTreeBuilder;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeConsoleFormatter;
import com.rebaze.tree.api.TreeSession;

import static com.rebaze.tree.api.Selector.selector;

import java.io.File;
import java.util.Map;

/**
 * Created by tonit on 04/11/15.
 */
public class FSScanner
{
    private final static TreeConsoleFormatter FORMAT = new TreeConsoleFormatter();

    public void walk( Map<Tree, Integer> map, Tree input )
    {
        if (TreeSession.isWrapper( input )) {
            // go deeper without counting:
            walk(map,input.branches()[0]);
            return;
        }

        if ( map.containsKey( input ) )
        {
            map.put( input, map.get( input) + 1 );
        }
        else
        {
            map.put( input, 1 );
        }
        for ( Tree tree : input.branches() )
        {
            walk( map, tree );
        }
    }

    public TreeBuilder collect( TreeBuilder builder, File base )
    {
        builder.selector( selector( base.getName() ) );
        for ( File f : base.listFiles() )
        {
            if ( f.isHidden() || !f.canRead() )
                continue;
            TreeBuilder sub = builder.branch( selector( f.getName() ) );
            if ( f.isDirectory() )
                collect( sub, f );
            else
            {
                new StreamTreeBuilder( sub ).add( f );
            }
        }
        return builder;
    }

}