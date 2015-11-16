package com.rebaze.autocode.internal.maven;

import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.api.core.AcceptsExtensions;
import com.rebaze.autocode.api.core.NativeSubjectHandler;

import java.io.File;

/**
 * Created by tonit on 03/11/15.
 */
public class MavenSubjectHandler implements NativeSubjectHandler, AcceptsExtensions
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

    @Override
    public void installExtension(BuildSubject extension) {
        //
    }

    @Override public String[] getEnv()
    {
        return ("M2_HOME=" + getInstallPath().getAbsolutePath()).split( " " );
    }

}
