package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.trees.core.Tree;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Can resolve things based on a given Treescape.
 *
 */
@Singleton
public class WorkspaceResolver implements ResourceResolver<GAV>, ResourceMaterializer
{

    @Inject
    public WorkspaceResolver(Tree surface) {
        // that tree makes up everything this resolver knows about.
        // Index tree using GAV->Tree->File
    }

    @Override public File get( Tree input )
    {
        // trees coming in here already have metadata for the file location, so its just about reveiling the info.
        return null;
    }

    @Override public Tree resolve( GAV query )
    {
        // Look up at the index.
        return null;
    }
}
