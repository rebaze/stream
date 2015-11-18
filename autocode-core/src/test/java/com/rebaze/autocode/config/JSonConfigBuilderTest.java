package com.rebaze.autocode.config;

import okio.Okio;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by tonit on 29/10/15.
 */
public class JSonConfigBuilderTest
{

    @Test
    public void testLoadConfiguration() throws IOException
    {
        WorkspaceConfiguration wconfig = getTestWorkspaceConfig();
        Configuration config = wconfig.getConfiguration();
        assertEquals( "Toni's local universe", config.getName() );
        assertEquals( "toni@rebaze.com", config.getOwner() );
        assertEquals( 1, config.getRepository().getSubjects().size() );
        assertEquals( "/tmp/autocode/cache", config.getRepository().getCache().getFolder() );
        assertEquals(1,wconfig.getResourceTreeConfiguration().getObjects().size());
        assertEquals("b8eebb7ba265532bf55aa17f89440c2fe267bed3",wconfig.getResourceTreeConfiguration().getObjects().get( 0 ).getNode());

    }

    public static WorkspaceConfiguration getTestWorkspaceConfig() throws IOException
    {
        return new JSonConfigBuilder(
                Okio.source( JSonConfigBuilderTest.class.getResourceAsStream( "/autocode-universe.json" ) ),
                Okio.source( JSonConfigBuilderTest.class.getResourceAsStream( "/autocode-locations.json" ) ),
                Okio.source( JSonConfigBuilderTest.class.getResourceAsStream( "/autocode-tree.json" ) )
                );
    }

}
