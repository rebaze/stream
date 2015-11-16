package com.rebaze.autocode.internal.maven;

import com.rebaze.autocode.config.AutocodeAddress;
import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.api.core.SubjectHandlerFactory;

import java.io.File;

/**
 * Created by tonit on 04/11/15.
 */
public class MavenSubjectHandlerFactory implements SubjectHandlerFactory
{
    public final static String TYPE = "maven3";


    @Override public MavenSubjectHandler create( AutocodeArtifact artifact, File path )
    {
        // TODO: here we can make sure that settings and extensions are installed properly.

        MavenSubjectHandler handler = new MavenSubjectHandler( path );
        for (AutocodeAddress ex : artifact.getExtensions()) {
            handler.installExtension( resolve(ex) );
        }
        return handler;
    }

    private BuildSubject resolve( AutocodeAddress ex )
    {
        return null;
    }

    @Override public boolean accept( BuildSubject subject )
    {
        return TYPE.equals(subject.getType());
    }
}
