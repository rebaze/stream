package com.rebaze.autocode.internal;

import com.rebaze.autocode.api.core.*;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.*;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.trees.core.Tree;
import okio.Okio;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static okio.Okio.buffer;

/**
 * Unpacks deliverables into a local cache.
 */
@Component
public class DefaultWorkspace implements Workspace
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultWorkspace.class );

    @Reference
    ResourceResolver<GAV> resolver;

    @Reference(name="compositeResolver")
    ResourceMaterializer materializer;

    @Reference
    List<SubjectHandlerFactory> handlerFactories;

    @Reference 
    WorkspaceConfiguration configuration;

    private Map<String, NativeSubjectHandler> map = new HashMap<>();

    
    public DefaultWorkspace() {
    	handlerFactories = new ArrayList<>();
    }
    // Unpack
    public void unpack() throws IOException
    {
        LOG.info( "Unpacking universe.." );
        // download all subjects
        Repository repository = configuration.getConfiguration().getRepository();
        for ( BuildSubject sub : repository.getSubjects() )
        {
            installSubject( sub );
        }

    }

    private void installSubject( BuildSubject subject ) throws IOException
    {
        SubjectHandlerFactory factory = selectHandlerFactory( subject );
        for ( SubjectVersion version : subject.getDistributions() )
        {
            for ( AutocodeArtifact artifact : version.getArtifacts() )
            {
                StagedSubject installed = install( getAddressFromArtifact( version, artifact ) );
                File base = unpackIfNew( installed );
                install( factory.create( this, artifact, unwrapFirstSublevel( base ) ) );
            }
        }
    }

    private AutocodeAddress getAddressFromArtifact( SubjectVersion version, AutocodeArtifact artifact )
    {
        return new AutocodeAddress( "gav", artifact.getCoordinates() + ":" + version.getVersion() );
    }

    @Override
    public StagedSubject install( AutocodeAddress address )
    {
        Tree tree = resolver.resolve( GAV.fromString( address.getData() ) );
        File f = materializer.get( tree );
        return new StagedSubject( address, tree, f );
    }

    private File unpackIfNew( StagedSubject installed )
    {
        LOG.info( "Installing " + installed );
        File base = new File( configuration.getConfiguration().getRepository().getCache().getFolder(), installed.getTree().fingerprint() );
        if ( base.exists() )
        {
            LOG.info( "{} is already installed.", installed );
        }
        else
        {
            base.mkdirs();
            extract( installed, base );
        }
        return base;
    }

    private void install( NativeSubjectHandler handler )
    {
        // process:

        this.map.put( handler.getType(), handler );
    }

    private File unwrapFirstSublevel( File base )
    {
        for ( File f : base.listFiles() )
        {
            if ( f.isDirectory() && !f.isHidden() )
            {
                return f;
            }
        }

        return base;
    }

    private SubjectHandlerFactory selectHandlerFactory( BuildSubject subject )
    {
        for ( SubjectHandlerFactory candidate : handlerFactories )
        {
            if ( candidate.accept( subject ) )
            {
                return candidate;
            }
        }
        throw new AutocodeException( "No handler available for subject type " + subject.getType() + "(tested: "+handlerFactories.size()+" services.)");
    }

    private void extract( StagedSubject installed, File base )
    {
        try ( ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( new BufferedInputStream( new FileInputStream( installed.getFile() ) ) ) )
        {
            ArchiveEntry entry;
            while ( ( entry = input.getNextEntry() ) != null )
            {
                File target = new File( base, entry.getName() );

                if ( entry.isDirectory() )
                {
                    target.mkdirs();
                }
                else
                {
                    buffer( Okio.source( input ) ).readAll( Okio.sink( target ) );
                }
            }
        }
        catch ( IOException | ArchiveException e )
        {
            LOG.error( "Problem unpacking archive " + installed + ".", e );
        }
    }

    @Override public NativeSubjectHandler get( String subjectType )
    {
        return map.get( subjectType );
    }

}
