package com.rebaze.autocode.core;

import com.google.inject.Provides;
import com.rebaze.autocode.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by tonit on 29/10/15.
 */
public class AutocodeBuilder
{

    private static Logger LOG = LoggerFactory.getLogger(AutocodeBuilder.class);

    @Inject WorkspaceConfiguration workspaceConfig;

    @Inject Set<AutocodeArtifactResolver> resolvers;

    @Inject SubjectRegistry subjectRegistry;

    @Provides
    public Autocode build() throws IOException
    {
        LOG.info("Unpacking universe..");
        // download all subjects
        for( BuildSubject sub : workspaceConfig.getConfiguration().getRepository().getSubjects()) {
            for ( SubjectVersion version : sub.getDistributions()) {
                for (AutocodeArtifact artifact : version.getArtifacts()) {
                    for (AutocodeArtifactResolver resolver : resolvers)
                    {
                        StagedSubject installed = resolver.download( artifact );
                        if ( installed != null )
                        {
                            subjectRegistry.install( installed );
                            break;
                        }
                    }
                }
            }
        }

        return new Autocode();
        // clone all subjects git

    }
}
