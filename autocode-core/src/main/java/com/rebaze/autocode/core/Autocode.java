package com.rebaze.autocode.core;

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
        ShellRunner runner = new ShellRunner( true );

        //runner.exec( path, "".split( " " ),"/Users/tonit/devel/build/bin/mvn clean install".split(" ") );

    }
}
