package com.rebaze.autocode.api.core;

import com.rebaze.autocode.internal.exec.ShellRunner;
import com.rebaze.autocode.internal.fs.FSScanner;
import com.rebaze.autocode.internal.DefaultEffect;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.ext.operators.DiffTreeCombiner;
import com.rebaze.trees.core.util.TreeConsoleFormatter;
import com.rebaze.trees.core.util.TreeSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * High level interface to Autocode.
 */
@Singleton
public class Autocode
{
    private final Workspace workspace;
    private final TreeSession session;

    @Inject
    public Autocode(Workspace workspace, TreeSession session) throws IOException
    {
        this.workspace = workspace;
        this.session = session;
        workspace.unpack();
    }

    public Effect build( File path )
    {
        // scan:
        Tree before = new FSScanner().collect( session.createTreeBuilder(), path  ).seal();

        // select appropriate builder and find it in registry:
        NativeSubjectHandler handler = workspace.get("maven3");

        ShellRunner runner = new ShellRunner( true );
        int res = runner.exec( path, handler.getEnv(),(handler.getExecutable().getAbsolutePath() +  " verify").split(" ") );

        Tree after = new FSScanner().collect( session.createTreeBuilder(), path  ).seal();
        Tree result = new DiffTreeCombiner(session).combine( before, after );

        List<File> diff = new ArrayList<>(  );

        // TODO: read output from extension to read execution plan from maven.

        if (result.branches().length > 0) {
            TreeConsoleFormatter format = new TreeConsoleFormatter(System.out);
            format.prettyPrint( result );
        }
        // TODO: Add output streams
        return new DefaultEffect(res,result);
    }


}
