package com.rebaze.autocode.registry;

import com.google.inject.Provides;
import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.config.SubjectVersion;
import com.rebaze.autocode.core.Autocode;
import com.rebaze.autocode.core.AutocodeArtifactResolver;
import com.rebaze.autocode.core.StagedSubject;
import com.rebaze.autocode.core.SubjectRegistry;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.maven.MavenSubjectHandler;
import okio.Okio;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static okio.Okio.buffer;

/**
 * Unpacks deliverables into a local cache.
 */
public class DefaultSubjectRegistry implements SubjectRegistry
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultSubjectRegistry.class );

    @Inject Set<AutocodeArtifactResolver> resolvers;
    @Inject WorkspaceConfiguration configuration;
    private Map<String, NativeSubjectHandler> map = new HashMap<>(  );


    // Unpack
    public void unpack() throws IOException
    {
        LOG.info("Unpacking universe..");
        // download all subjects
        for( BuildSubject sub : configuration.getConfiguration().getRepository().getSubjects()) {
            for ( SubjectVersion version : sub.getDistributions()) {
                for (AutocodeArtifact artifact : version.getArtifacts()) {
                    for (AutocodeArtifactResolver resolver : resolvers)
                    {
                        StagedSubject installed = resolver.download( artifact );
                        if ( installed != null )
                        {
                            install( installed );
                            break;
                        }
                    }
                }
            }
        }

    }

    private void install( StagedSubject installed )
    {
        LOG.info("Installing " + installed);
        File base = new File( configuration.getConfiguration().getRepository().getCache().getFolder(), installed.getArtifact().getChecksum().getData() );
        if (base.exists()) {
            LOG.info("{} is already installed.",installed);
        }else {
            base.mkdirs();
            extract( installed, base );
        }
        // Now that it is extracted..
        // select the "processor" for type:

        install(selectHandlerFromInstalledBase(unwrapFirstSublevel(base)));

    }

    private void install( NativeSubjectHandler handler )
    {
        this.map.put(handler.getType(),handler);
    }

    private File unwrapFirstSublevel( File base )
    {
        for (File f : base.listFiles()) {
            if (f.isDirectory() && !f.isHidden()) {
                return f;
            }
        }

        return base;
    }

    private NativeSubjectHandler selectHandlerFromInstalledBase( File base )
    {
        return new MavenSubjectHandler(base);
    }

    private void extract( StagedSubject installed, File base )
    {
        try ( ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( new BufferedInputStream( new FileInputStream( installed.getFile() ) ) ) )
        {
            ArchiveEntry entry;
            while ( ( entry = input.getNextEntry() ) != null )
            {
                File target = new File( base, entry.getName() );

                if (entry.isDirectory()) {
                    target.mkdirs();
                }else
                {
                    buffer( Okio.source( input ) ).readAll( Okio.sink( target ) );
                }
            }
        }catch(IOException | ArchiveException e) {
            LOG.error("Problem unpacking archive " + installed + ".",e);
        }
    }

    @Override public NativeSubjectHandler get( String subjectType )
    {
        return map.get(subjectType);
    }
}
