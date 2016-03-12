package com.rebaze.autocode.api.core;

import java.io.IOException;

/**
 * Created by tonit on 16/11/15.
 */
public interface Workspace
{
    void unpack() throws IOException;

    StagedSubject install( AutocodeAddress address );

    NativeSubjectHandler get( String subjectType );
}
