package com.rebaze.autocode.exec;

import org.ops4j.exec.CommandLineBuilder;
import org.ops4j.exec.ExecutionException;
import org.ops4j.io.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tonit on 23/10/15.
 */
public class ShellRunner
{

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( ShellRunner.class );
    public static final String SHELL_RUNNER_SHUTDOWN_HOOK = "Shell Runner shutdown hook";

    /**
     * If the execution should wait for platform shutdown.
     */
    private final boolean m_wait;
    /**
     * Framework process.
     */
    private Process m_frameworkProcess;
    /**
     * Shutdown hook.
     */
    private Thread m_shutdownHook;

    /**
     * Constructor.
     */
    public ShellRunner()
    {
        this( true );
    }

    /**
     * Constructor.
     *
     * @param wait should wait for framework exists
     */
    public ShellRunner( boolean wait )
    {
        m_wait = wait;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void exec( File workingDirectory, String[] envOptions, String[] cmd )
        throws ExecutionException
    {
        final CommandLineBuilder commandLine = new CommandLineBuilder();
        commandLine.append( cmd );

        if ( m_frameworkProcess != null )
        {
            throw new ExecutionException( "Platform already started" );
        }

        LOG.debug( "Start command line [" + Arrays.toString( commandLine.toArray() ) + "]" );

        try
        {
            LOG.debug( "Starting platform process." );
            m_frameworkProcess = Runtime.getRuntime().exec( commandLine.toArray(), createEnvironmentVars( envOptions ), workingDirectory );
        }
        catch ( IOException e )
        {
            throw new ExecutionException( "Could not start up the process", e );
        }

        m_shutdownHook = createShutdownHook( m_frameworkProcess );
        Runtime.getRuntime().addShutdownHook( m_shutdownHook );

        LOG.debug( "Added shutdown hook." );
        LOG.info( "DefaultJavaRunner completed successfully" );

        if ( m_wait )
        {
            waitForExit();
        }
        else
        {
            System.out.println();
        }
    }

    private String[] createEnvironmentVars( String[] envOptions )
    {
        List<String> env = new ArrayList<>();
        Map<String, String> getenv = System.getenv();
        env.addAll( getenv.keySet().stream().map( key -> key + "=" + getenv.get( key ) ).collect( Collectors.toList() ) );
        if ( envOptions != null )
            Collections.addAll( env, envOptions );
        return env.toArray( new String[env.size()] );
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown()
    {
       try
        {
            if ( m_shutdownHook != null )
            {
                synchronized ( m_shutdownHook )
                {
                    if ( m_shutdownHook != null )
                    {
                        LOG.debug( "Shutdown in progress..." );
                        Runtime.getRuntime().removeShutdownHook( m_shutdownHook );
                        m_frameworkProcess = null;
                        m_shutdownHook.run();
                        m_shutdownHook = null;
                        LOG.info( "Platform has been shutdown." );
                    }
                }
            }
        }
        catch ( IllegalStateException ignore )
        {
            // just ignore
        }
    }

    /**
     * Wait till the framework process exits.
     */
    public void waitForExit()
    {
        synchronized ( m_frameworkProcess )
        {
            try
            {
                LOG.debug( "Waiting for framework exit." );
                System.out.println();
                m_frameworkProcess.waitFor();
                shutdown();
            }
            catch ( Throwable e )
            {
                LOG.debug( "Early shutdown.", e );
                shutdown();
            }
        }
    }

    /**
     * Create helper thread to safely shutdown the external framework process
     *
     * @param process framework process
     * @return stream handler
     */
    private Thread createShutdownHook( final Process process )
    {
        LOG.debug( "Wrapping stream I/O." );

        final Pipe errPipe = new Pipe( process.getErrorStream(), System.err ).start( "Error pipe" );
        final Pipe outPipe = new Pipe( process.getInputStream(), System.out ).start( "Out pipe" );
        final Pipe inPipe = new Pipe( process.getOutputStream(), System.in ).start( "In pipe" );

        return new Thread(
            () -> {
                System.out.println();
                LOG.debug( "Unwrapping stream I/O." );

                inPipe.stop();
                outPipe.stop();
                errPipe.stop();

                try
                {
                    process.destroy();
                }
                catch ( Exception e )
                {
                    // ignore if already shutting down
                }
            },
            SHELL_RUNNER_SHUTDOWN_HOOK
        );
    }

    /**
     * Return path to java executable.
     *
     * @param javaHome java home directory
     * @return path to java executable
     * @throws ExecutionException if java home could not be located
     */
    static String getJavaExecutable( final String javaHome )
        throws ExecutionException
    {
        if ( javaHome == null )
        {
            throw new ExecutionException( "JAVA_HOME is not set." );
        }
        return javaHome + "/bin/java";
    }

}
