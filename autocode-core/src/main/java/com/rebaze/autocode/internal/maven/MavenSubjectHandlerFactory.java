package com.rebaze.autocode.internal.maven;

import com.rebaze.autocode.api.core.SubjectHandlerFactory;
import com.rebaze.autocode.api.core.Workspace;
import com.rebaze.autocode.config.AutocodeAddress;
import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;

import javax.inject.Inject;
import java.io.File;

/**
 * Created by tonit on 04/11/15.
 */
public class MavenSubjectHandlerFactory implements SubjectHandlerFactory
{
    public final static String TYPE = "maven3";

    @Inject Workspace workspace;

    @Override public MavenSubjectHandler create( AutocodeArtifact artifact, File path )
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
