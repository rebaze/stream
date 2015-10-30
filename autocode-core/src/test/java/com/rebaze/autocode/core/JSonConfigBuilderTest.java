package com.rebaze.autocode.core;

import com.rebaze.autocode.api.Configuration;
import okio.Okio;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

/**
 * Created by tonit on 29/10/15.
 */
public class JSonConfigBuilderTest
{

    @Test
    public void testLoadConfiguration() throws IOException
    {
        JSonConfigBuilder builder = new JSonConfigBuilder( Okio.source( getClass().getResourceAsStream( "/autocode-universe.json" ) ),
            Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) ) );
        Configuration config = builder.getConfiguration();
        assertEquals( "Toni's local universe", config.getName() );
        assertEquals( "toni@rebaze.com", config.getOwner() );
        assertEquals( 1, config.getRepository().getSubjects().size() );

        assertEquals( "/tmp/autocode/cache", config.getRepository().getCache().getFolder() );

    }

}
