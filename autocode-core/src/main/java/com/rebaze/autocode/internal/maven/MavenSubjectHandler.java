package com.rebaze.autocode.internal.maven;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.core.StagedSubject;
import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.api.core.AcceptsExtensions;
import com.rebaze.autocode.api.core.NativeSubjectHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
    public void installExtension(StagedSubject extension) {
        // Either copy the extension to lib/ext
        // or make sure that every "run" has a properly configured .mvn/extensions.xml
        try
        {
            Files.copy(extension.getFile().toPath(),new File(base,"/lib/ext/" + extension.getTree().fingerprint() + ".jar").toPath());
        }
        catch ( IOException e )
        {
            throw new AutocodeException( "Cannot install extension " + extension );
        }
    }

    @Override public String[] getEnv()
    {
        return ("M2_HOME=" + getInstallPath().getAbsolutePath()).split( " " );
    }

}
