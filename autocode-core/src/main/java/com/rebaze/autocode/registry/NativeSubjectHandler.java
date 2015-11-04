package com.rebaze.autocode.registry;

import java.io.File;

/**
 * Created by tonit on 03/11/15.
 */
public interface NativeSubjectHandler
{
    String getType();

    File getInstallPath();

    File getExecutable();

    String[] getEnv();

}
