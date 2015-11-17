package com.rebaze.autocode.internal;

import com.rebaze.autocode.api.core.*;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.api.transport.Workspace;
import com.rebaze.autocode.api.transport.WorkspaceException;
import com.rebaze.autocode.config.*;
import com.rebaze.commons.tree.Tree;
import okio.Okio;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Singleton
public class DefaultSubjectRegistry implements SubjectRegistry, Workspace
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultSubjectRegistry.class );

    @Inject ResourceResolver<GAV> resolver;

    @Inject ResourceMaterializer materializer;

    @Inject Set<SubjectHandlerFactory> handlerFactories;

    @Inject WorkspaceConfiguration configuration;
    private Map<String, NativeSubjectHandler> map = new HashMap<>(  );

    // Unpack
    public void unpack() throws IOException
    {
        LOG.info("Unpacking universe..");
        // download all subjects
        Repository repository = configuration.getConfiguration().getRepository();
        for( BuildSubject sub : repository.getSubjects()) {
            installSubject( sub );
        }

    }

    private void installSubject( BuildSubject subject ) throws IOException
    {
        SubjectHandlerFactory factory = selectHandlerFactory( subject );
        for ( SubjectVersion version : subject.getDistributions()) {
            for (AutocodeArtifact artifact : version.getArtifacts()) {
                Tree tree = resolver.resolve( GAV.fromString( artifact.getCoordinates() + ":" + version.getVersion() ) );
                File f = materializer.get( tree );
                install (factory, new StagedSubject( artifact,tree, f ));
            }
        }
    }

    private void install(  SubjectHandlerFactory factory, StagedSubject installed )
    {
        LOG.info("Installing " + installed);
        File base = new File( configuration.getConfiguration().getRepository().getCache().getFolder(), installed.getTree().fingerprint() );
        if (base.exists()) {
            LOG.info("{} is already installed.",installed);
        }else {
            base.mkdirs();
            extract( installed, base );
        }
        // Now that it is extracted..
        // select the "processor" for type:

        install(factory.create( installed.getArtifact(),unwrapFirstSublevel(base)));

    }

    private void install( NativeSubjectHandler handler )
    {
        // process:

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

    private SubjectHandlerFactory selectHandlerFactory( BuildSubject subject )
    {
        for (SubjectHandlerFactory candidate : handlerFactories) {
            if (candidate.accept(subject)) {
                return candidate;
            }
        }
        throw new AutocodeException("No handler available for subject " + subject);
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

    @Override public File locate( Tree tree ) throws WorkspaceException
    {
        return null;
    }

}
