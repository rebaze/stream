package com.rebaze.autocode.api.transport;

import java.io.File;

import com.rebaze.tree.api.Tree;

/**
 * Thing that can materialize a given tree into a workspace.
 *
 */
public interface ResourceMaterializer
{
    File get(Tree input);
}
