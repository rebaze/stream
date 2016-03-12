/*
 * Copyright (c) 2015 rebaze GmbH
 * All rights reserved.
 *
 * This library and the accompanying materials are made available under the terms of the Apache License Version 2.0,
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.
 *
 */
package com.rebaze.trees.ext.operators;

import com.rebaze.trees.core.Tree;
import com.rebaze.trees.core.TreeBuilder;
import com.rebaze.trees.core.TreeCombiner;
import com.rebaze.trees.core.TreeSession;

import static com.rebaze.trees.core.Tag.tag;

/**
 * Covers merges of similar trees so that combiners work as a system. (see test cases about combiner integrity)
 * In a sense this should be really coverable by native {@link TreeBuilder#branch(tree)} .
 *
 * @author Toni Menzel (rebaze)
 * @since 0.3
 */
public class UnionTreeCombiner implements TreeCombiner
{
    private final TreeSession session;

    public UnionTreeCombiner( TreeSession session )
    {
        this.session = session;
    }

    @Override public Tree combine( Tree left, Tree right )
    {
        TreeBuilder builder = session.createTreeBuilder().tag( tag( "UNION" ) );
        include( builder, left );
        include( builder, right );
        return builder.seal();
    }

    private void include( TreeBuilder collector, Tree left )
    {

        if ( left.branches().length == 0 )
        {
            collector.branch( left );
        }
        else
        {
            TreeBuilder subBuilder = collector.branch( left ).tag( tag( "infix" ) );
            /**
             for ( Tree tree : left.branches() )
             {
             include( subBuilder, tree );
             }
             **/
        }
    }
}
