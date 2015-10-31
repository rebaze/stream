package com.rebaze.autocode.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
        autocode.build( new File( "/Users/tonit/devel/org.ops4j.base" ) );
    }

}
