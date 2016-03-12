package com.rebaze.autocode.internal;

import com.rebaze.autocode.api.core.Effect;
import com.rebaze.trees.core.Tree;

/**
 * Created by tonit on 04/11/15.
 */
public class DefaultEffect implements Effect
{
    private final int returnCode;
    private final Tree tree;

    public DefaultEffect( int res, Tree diff )
    {
        this.tree = diff;
        this.returnCode = res;
    }

    @Override public int getReturnCode()
    {
        return returnCode;
    }

    @Override public Tree getTree()
    {
        return tree;
    }
}
