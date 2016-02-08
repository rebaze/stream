package com.rebaze.autocode.internal.maven;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.core.StagedSubject;
import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.api.core.AcceptsExtensions;
import com.rebaze.autocode.api.core.NativeSubjectHandler;
import org.ops4j.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;

/**
 * Created by tonit on 03/11/15.
 */
public class MavenSubjectHandler implements NativeSubjectHandler, AcceptsExtensions
{
    private final Logger LOG = LoggerFactory.getLogger( MavenSubjectHandler.class );
    private final File base;
    private final File extensionsFolder;

    public MavenSubjectHandler( File base )
    {
        this.base = base;
        // prune existing extensions:
        extensionsFolder = new File( base, "lib/ext/" );
        FileUtils.delete( extensionsFolder );
        extensionsFolder.mkdirs();

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
            File targetName = new File( extensionsFolder, extension.getTree().fingerprint() + ".jar" );

            if (!targetName.exists())
            {
                LOG.info( "Installing extension " + targetName.getName() );
                Files.copy(extension.getFile().toPath(), targetName.toPath());
            }
        }
        catch ( IOException e )
        {
            throw new AutocodeException( "Cannot install extension " + extension,e );
        }
    }

    @Override public String[] getEnv()
    {
        return ("M2_HOME=" + getInstallPath().getAbsolutePath()).split( " " );
    }

}
