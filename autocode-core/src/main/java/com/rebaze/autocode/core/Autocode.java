package com.rebaze.autocode.core;

import com.rebaze.autocode.exec.ShellRunner;
import com.rebaze.autocode.fs.FSScanner;
import com.rebaze.autocode.internal.DefaultEffect;
import com.rebaze.autocode.registry.NativeSubjectHandler;
import com.rebaze.commons.tree.Tree;
import com.rebaze.commons.tree.operators.DiffTreeCombiner;
import com.rebaze.commons.tree.util.TreeConsoleFormatter;
import com.rebaze.commons.tree.util.TreeSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/**
 * High level interface to Autocode.
 */
@Singleton
public class Autocode
{
    private final SubjectRegistry registry;
    private final TreeSession session;

    @Inject
    public Autocode(SubjectRegistry registry, TreeSession session) throws IOException
    {
        this.registry = registry;
        this.session = session;
        registry.unpack();
    }

    public Effect build( File path )
    {
        // scan:

        Tree before = new FSScanner().collect( session.createTreeBuilder(), path  ).seal();

        // select appropriate builder and find it in registry:
        NativeSubjectHandler handler = registry.get("maven3");

        ShellRunner runner = new ShellRunner( true );
        int res = runner.exec( path, handler.getEnv(),(handler.getExecutable().getAbsolutePath() +  " verify").split(" ") );

        Tree after = new FSScanner().collect( session.createTreeBuilder(), path  ).seal();
        Tree result = new DiffTreeCombiner(session).combine( before, after );

        // TODO: read output from extension to read execution plan from maven.

        if (result.branches().length > 0) {
            TreeConsoleFormatter format = new TreeConsoleFormatter(System.out);
            format.prettyPrint( result );
        }
        // TODO: Add output streams
        return new DefaultEffect(res,result);
    }


}
