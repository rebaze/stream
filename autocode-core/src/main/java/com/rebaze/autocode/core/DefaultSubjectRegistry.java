package com.rebaze.autocode.core;

import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
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

import static okio.Okio.buffer;

/**
 * Unpacks deliverables into a local cache.
 */
public class DefaultSubjectRegistry implements SubjectRegistry
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultSubjectRegistry.class );

    @Inject WorkspaceConfiguration configuration;

    @Override public void install( StagedSubject installed )
    {
        LOG.info("Installing " + installed);
        try ( ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( new BufferedInputStream( new FileInputStream( installed.getFile() ) ) ) )
        {
            ArchiveEntry entry;
            while ( ( entry = input.getNextEntry() ) != null )
            {
                File target = new File( configuration.getConfiguration().getRepository().getCache().getFolder(), entry.getName() );
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
}
