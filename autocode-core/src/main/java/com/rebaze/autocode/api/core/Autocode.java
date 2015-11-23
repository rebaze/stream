package com.rebaze.autocode.api.core;

import com.rebaze.autocode.internal.exec.ShellRunner;
import com.rebaze.autocode.internal.fs.FSScanner;
import com.rebaze.autocode.internal.DefaultEffect;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.ext.operators.DiffTreeCombiner;
import com.rebaze.trees.core.util.TreeConsoleFormatter;
import com.rebaze.trees.core.TreeSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * High level interface to Autocode.
 * It can:
 * - BUILD: Reliably build projects across build technology and configuration.
 * - OPTIMIZE: Optimize the underlying build descriptor.
 * - SHARE: Ability to -repackage underlying project into a form that can be reproduced from an OSS community without leaking corporate data.
 */
@Singleton
public class Autocode
{
    private final Workspace workspace;
    private final TreeSession session;
    private  boolean initialized = false;

    @Inject
    public Autocode( Workspace workspace, TreeSession session )
    {
        this.workspace = workspace;
        this.session = session;
    }

    public synchronized void unpack() throws IOException
    {
        if ( !initialized )
        {
            workspace.unpack();
            initialized = true;
        }
    }

    public void configure() throws IOException
    {
        unpack();
    }

    public Effect build( File path ) throws IOException
    {
        unpack();
        // scan:
        Tree before = new FSScanner().collect( session.createTreeBuilder(), path ).seal();

        //

        NativeSubjectHandler handler = selectHandlerForBase(path); //

        OutputStream out = new FileOutputStream( new File( "target/out.txt" ) ); // TODO: Tee to event listener in order to grab messages on the fly.
        InputStream in = null; // Todo: do not accept input from here now.

        Integer res = null;
        try
        {
            ShellRunner runner = new ShellRunner( out, in, true );
            System.out.print("Building.. hold your breath..");
            System.out.println();

            System.out.flush();
            res = runner.exec( path, handler.getEnv(), ( handler.getExecutable().getAbsolutePath() + " verify" ).split( " " ) );
        }
        finally
        {
            try
            {
                out.close();
            }
            catch ( IOException e )
            {

            }
        }
        if (res == 0) {
            System.out.println("SUCCESS!");
        }else {
            System.out.println("FAILED!");
        }
        Tree after = new FSScanner().collect( session.createTreeBuilder(), path ).seal();
        Tree result = new DiffTreeCombiner( session ).combine( before, after );

        List<File> diff = new ArrayList<>();

        // TODO: read output from extension to read execution plan from maven.

        if ( result.branches().length > 0 )
        {
            TreeConsoleFormatter format = new TreeConsoleFormatter( System.out );
            format.prettyPrint( result );
        }
        // TODO: Add output streams
        return new DefaultEffect( res, result );
    }

    public void modify() {

    }

    public void share() {

    }

    // Simplified version.. ;)
    private NativeSubjectHandler selectHandlerForBase( File path )
    {
        if (new File(path,"pom.xml").exists())
        {
            NativeSubjectHandler handler = workspace.get("maven3");
            if (handler == null) {
                throw new AutocodeException( "No Maven handler found to build " + path.getAbsolutePath() );
            } else
            {
                return handler;
            }
        }else {
            throw new AutocodeException( "No subject handler to perform build on project " + path.getAbsolutePath());
        }
    }

}
