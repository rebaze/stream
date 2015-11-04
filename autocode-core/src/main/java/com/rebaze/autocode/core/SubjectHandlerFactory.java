package com.rebaze.autocode.core;

import com.rebaze.autocode.config.BuildSubject;
import com.rebaze.autocode.registry.NativeSubjectHandler;

import java.io.File;

/**
 * Created by tonit on 04/11/15.
 */
public interface SubjectHandlerFactory
{
    NativeSubjectHandler create(File path);

    boolean accept( BuildSubject subject );
}