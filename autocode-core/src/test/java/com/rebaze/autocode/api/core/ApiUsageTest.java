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
    public void testBuildSimpleMavenProject() throws IOException
    {
        Autocode autocode = getAutocode();
        File base = new File( "src/test/resources/maven/project1" );
        Effect build = autocode.build( base );

        assertEquals("Executor should return normally",0, build.getReturnCode());
        assertTrue(new File(base,"/target/autocode.json").exists());
        //assertEquals("No change in filesystem",0, TreeSession.leafs( build.getTree()) );
    }

    @Test(expected = AutocodeException.class)
    public void testBuildUnsupportedProject() throws IOException
    {
        Autocode autocode = getAutocode();
        autocode.build( new File( "src/test/resources/gradle/project2" ) );
    }

    private Autocode getAutocode()
    {
        Injector injector = Guice.createInjector( new TestConfigurationModule(), new DefaultModule() );
        return injector.getInstance( Autocode.class );
    }
}
