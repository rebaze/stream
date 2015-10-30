package com.rebaze.autocode.core;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by tonit on 29/10/15.
 */
public interface SubjectRegistry
{
    void install( StagedSubject installed );
}
