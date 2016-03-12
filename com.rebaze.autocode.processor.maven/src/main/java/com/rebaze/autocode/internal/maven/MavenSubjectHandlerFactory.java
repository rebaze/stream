package com.rebaze.autocode.internal.maven;

import java.io.File;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.autocode.api.core.AutocodeAddress;
import com.rebaze.autocode.api.core.SubjectHandlerFactory;
import com.rebaze.autocode.api.core.Workspace;
import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;

/**
 * Created by tonit on 04/11/15.
 */
@Component
public class MavenSubjectHandlerFactory implements SubjectHandlerFactory
{
    public final static String TYPE = "maven3";

    @Override public MavenSubjectHandler create( Workspace workspace, AutocodeArtifact artifact, File path )
    {
        MavenSubjectHandler handler = new MavenSubjectHandler( path );
        for (AutocodeAddress ex : artifact.getExtensions()) {
            handler.installExtension( workspace.install( ex ) );
        }
        return handler;
    }

    @Override public boolean accept( BuildSubject subject )
    {
        return TYPE.equals(subject.getType());
    }
}
