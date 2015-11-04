package com.rebaze.autocode.maven;

import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.core.SubjectHandlerFactory;

import java.io.File;

/**
 * Created by tonit on 04/11/15.
 */
public class MavenSubjectHandlerFactory implements SubjectHandlerFactory
{
    public final static String TYPE = "maven3";

    @Override public MavenSubjectHandler create( File path )
    {
        // TODO: here we can make sure that settings and extensions are installed properly.
        return new MavenSubjectHandler( path );
    }

    @Override public boolean accept( BuildSubject subject )
    {
        return TYPE.equals(subject.getType());
    }
}
