package com.rebaze.autocode.api.transport;

import com.rebaze.commons.tree.Tree;

/**
 * Thing that can materialize a given tree into a workspace.
 *
 */
public interface ResourceMaterializer
{
    void get(Tree input, Workspace output);

}
