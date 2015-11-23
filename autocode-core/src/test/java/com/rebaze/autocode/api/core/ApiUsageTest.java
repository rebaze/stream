package com.rebaze.autocode.api.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rebaze.autocode.internal.DefaultModule;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Autocode
 */
public class ApiUsageTest
{
    @Test
    public void testSimple() throws IOException
    {
        Autocode autocode = getAutocode();
        File base = new File( "/Users/tonit/devel/org.ops4j.base" );
        Effect build = autocode.build( base );

        assertEquals("Executor should return normally",0, build.getReturnCode());
        assertTrue(new File(base,"/target/autocode.json").exists());
        //assertEquals("No change in filesystem",0, TreeSession.leafs( build.getTree()) );
    }

    private Autocode getAutocode()
    {
        Injector injector = Guice.createInjector( new TestConfigurationModule(), new DefaultModule() );
        return injector.getInstance( Autocode.class );
    }
}
