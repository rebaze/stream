package com.rebaze.autocode.api.transport;

import com.rebaze.commons.tree.Tree;

import java.io.File;

/**
 * Thing that can materialize a given tree into a workspace.
 *
 */
public interface ResourceMaterializer
{
    File get(Tree input);
}
