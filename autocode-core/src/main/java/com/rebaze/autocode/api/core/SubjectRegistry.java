package com.rebaze.autocode.api.core;

import java.io.IOException;

/**
 * Created by tonit on 29/10/15.
 */
public interface SubjectRegistry
{
    void unpack() throws IOException;

    NativeSubjectHandler get( String subjectType );
}
