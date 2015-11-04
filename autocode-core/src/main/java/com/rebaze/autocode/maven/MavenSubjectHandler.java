package com.rebaze.autocode.maven;

import com.rebaze.autocode.registry.NativeSubjectHandler;

import java.io.File;

/**
 * Created by tonit on 03/11/15.
 */
public class MavenSubjectHandler implements NativeSubjectHandler
{
    private final File base;

    public MavenSubjectHandler( File base )
    {
        this.base = base;
    }

    @Override public String getType()
    {
        return "maven3";
    }

    @Override public File getInstallPath()
    {
        return base;
    }

    @Override public File getExecutable()
    {

        File f = new File(base,"/bin/mvn");
        f.setExecutable( true );
        return f;
    }

    @Override public String[] getEnv()
    {
        return ("M2_HOME=" + getInstallPath().getAbsolutePath()).split( " " );
    }
}
