package com.rebaze.autocode.core;

import com.google.inject.Provides;
import com.rebaze.autocode.config.AutocodeArtifact;
import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.config.SubjectVersion;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

/**
 * Created by tonit on 29/10/15.
 */
public class AutocodeBuilder
{

    private static Logger LOG = LoggerFactory.getLogger(AutocodeBuilder.class);

    @Inject WorkspaceConfiguration workspaceConfig;


    @Inject SubjectRegistry subjectRegistry;


}
