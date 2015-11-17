package com.rebaze.autocode.api.transport;

import com.rebaze.commons.tree.Tree;

import java.io.File;

/**
 * Created by tonit on 16/11/15.
 */
public interface Workspace
{
    /**
     * Will locatte the given leaf on filesystem.
     *
     * @param tree a leaf part of a tree.
     * @return location on file system or an exception.
     * @throws WorkspaceException in case the resource is not available.
     */
    File locate(Tree tree) throws WorkspaceException;
}
