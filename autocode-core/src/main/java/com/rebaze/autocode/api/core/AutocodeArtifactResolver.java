package com.rebaze.autocode.api.core;

import com.rebaze.autocode.config.AutocodeArtifact;

import java.io.IOException;

/**
 *
 */
public interface AutocodeArtifactResolver
{
    boolean accept(String protocol);

    StagedSubject download( AutocodeArtifact artifact ) throws IOException;
}
