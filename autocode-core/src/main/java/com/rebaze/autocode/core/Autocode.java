package com.rebaze.autocode.core;

import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.exec.ShellRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * High level interface to Autocode.
 */
@Singleton
public class Autocode
{
    @Inject WorkspaceConfiguration config;

    public void build( File path )
    {
        // build the toolchain:

        ShellRunner runner = new ShellRunner( true );

        //runner.exec( path, "".split( " " ),"/Users/tonit/devel/build/bin/mvn clean install".split(" ") );

    }
}
