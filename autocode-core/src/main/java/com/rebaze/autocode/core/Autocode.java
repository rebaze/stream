package com.rebaze.autocode.core;

import com.rebaze.autocode.exec.ShellRunner;
import com.rebaze.autocode.registry.NativeSubjectHandler;

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

    @Inject
    public Autocode(SubjectRegistry registry) throws IOException
    {
        this.registry = registry;
        registry.unpack();
    }

    public int build( File path )
    {
        // select appropriate builder and find it in registry:
        NativeSubjectHandler handler = registry.get("maven3");
        ShellRunner runner = new ShellRunner( true );
        return runner.exec( path, handler.getEnv(),(handler.getExecutable().getAbsolutePath() +  " -v").split(" ") );

        // consume result?

    }
}
