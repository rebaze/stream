package com.rebaze.autocode.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.Autocode;
import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.core.Effect;
import com.rebaze.autocode.api.core.NativeSubjectHandler;
import com.rebaze.autocode.api.core.SimpleAutocodeRemoteChannel;
import com.rebaze.autocode.api.core.Workspace;
import com.rebaze.autocode.internal.exec.ShellRunner;
import com.rebaze.autocode.internal.fs.FSScanner;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.trees.core.internal.TreeConsoleFormatter;
import com.rebaze.trees.ext.operators.DiffTreeCombiner;

/**
 * High level interface to Autocode.
 * It can:
 * - BUILD: Reliably build projects across build technology and configuration.
 * - OPTIMIZE: Optimize the underlying build descriptor.
 * - SHARE: Ability to -repackage underlying project into a form that can be reproduced from an OSS community without leaking corporate data.
 */
@Component
public class DefaultAutocode implements Autocode
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultAutocode.class );

    @Reference
    private Workspace workspace;
    
    @Reference(cardinality=ReferenceCardinality.OPTIONAL)
    private TreeSession session;
    
    private  boolean initialized = false;
    private volatile Remote remoteStub;

    @Activate
    public synchronized void unpack() throws IOException
    {
        if ( !initialized )
        {
            workspace.unpack();
            initialized = true;
        }
    }

    /* (non-Javadoc)
	 * @see com.rebaze.autocode.internal.Autocode#build(java.io.File)
	 */
    @Override
	public Effect build( File path ) throws IOException
    {
        // scan:
        LOG.info("Creating surface..");
        Tree before = new FSScanner().collect( session.createTreeBuilder(), path ).seal();
        //

        Integer res = execBuild( path );
        LOG.info("Creating footprint analysis..");

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

    private Integer execBuild( File path ) throws FileNotFoundException
    {
        NativeSubjectHandler handler = selectHandlerForBase(path); //

        File target = new File(path,"target");
        target.mkdirs();
        OutputStream out = new FileOutputStream( new File( target,"autocode.log" ) ); // TODO: Tee to event listener in order to grab messages on the fly.
        InputStream in = null; // Todo: do not accept input from here now.

        Integer res = null;
        Registry registry = null;
        ShellRunner runner = new ShellRunner( out, in, true );

        try
        {
            registry = LocateRegistry.createRegistry( 9981 );
            SimpleAutocodeRemoteChannel channel = new SimpleAutocodeRemoteChannel();
            remoteStub = UnicastRemoteObject.exportObject(channel , 0);
            registry.rebind("autocode", remoteStub);
            System.out.print("Building.. hold your breath..");
            System.out.println();
            System.out.flush();
            res = runner.exec( path, handler.getEnv(), ( handler.getExecutable().getAbsolutePath() + " verify" ).split( " " ) );
        }
        catch ( Exception e )
        {
            LOG.error("Problem..", e);
        }
        finally
        {

            unbind( registry );
            safeClose( out );
            runner.shutdown();
        }
        if (res == 0) {
            System.out.println("SUCCESS!");
        }else {
            System.out.println("FAILED!");
        }
        return res;
    }

    private void safeClose( OutputStream out )
    {
        try
        {
            out.close();
        }
        catch ( IOException e )
        {

        }
    }

    private void unbind( Registry registry )
    {
        try
        {
            for (String s : registry.list() ) {
                LOG.info("Unbind " + s);
                registry.unbind( s );
                Remote remote = registry.lookup(s);
                if (remote instanceof UnicastRemoteObject)
                {
                    UnicastRemoteObject.unexportObject( remote, true );
                }
            }
            registry = null;
            remoteStub = null;
        }catch(Exception e) {
            // don't care
        }
    }

    /* (non-Javadoc)
	 * @see com.rebaze.autocode.internal.Autocode#modify()
	 */
    @Override
	public void modify() {

    }

    /* (non-Javadoc)
	 * @see com.rebaze.autocode.internal.Autocode#share()
	 */
    @Override
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
