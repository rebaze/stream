package com.rebaze.autocode.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rebaze.autocode.internal.DefaultModule;
import com.rebaze.commons.tree.util.TreeSession;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * Autocode
 */
public class ApiUsageTest
{
    @Test
    public void testSimple() throws IOException
    {
        Injector injector = Guice.createInjector( new TestConfigurationModule(), new DefaultModule() );
        Autocode autocode  = injector.getInstance( Autocode.class );

        // build:
        Effect build = autocode.build( new File( "/Users/tonit/devel/org.ops4j.base" ) );
        assertEquals("Executor should return normally",0, build.getReturnCode());
        assertEquals("No change in filesystem",0, TreeSession.leafs( build.getTree()) );
    }
}
