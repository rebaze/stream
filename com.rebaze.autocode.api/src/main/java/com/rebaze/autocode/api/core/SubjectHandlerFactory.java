package com.rebaze.autocode.api.core;

import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;

import java.io.File;

/**
 * Created by tonit on 04/11/15.
 */
public interface SubjectHandlerFactory
{
    NativeSubjectHandler create( Workspace workspace, AutocodeArtifact artifact, File path );

    boolean accept( BuildSubject subject );
}
