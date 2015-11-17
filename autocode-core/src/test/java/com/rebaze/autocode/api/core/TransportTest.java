package com.rebaze.autocode.api.core;

import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.api.transport.Workspace;
import com.rebaze.autocode.config.JSonConfigBuilder;
import com.rebaze.autocode.config.JSonConfigBuilderTest;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.autocode.internal.transports.DefaultMaterializer;
import com.rebaze.autocode.internal.transports.StaticGAVResolver;
import com.rebaze.commons.tree.Tree;
import com.rebaze.commons.tree.TreeSessionFactory;
import com.rebaze.commons.tree.util.DefaultTreeSessionFactory;
import com.rebaze.commons.tree.util.TreeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by tonit on 16/11/15.
 */
public class TransportTest
{
    private TreeSession session;
    private ResourceTreeConfiguration config;

    @Before
    public void before() throws IOException
    {
        config = JSonConfigBuilderTest.getTestWorkspaceConfig().getResourceTreeConfiguration();
        session = new DefaultTreeSessionFactory().create();
    }

    @After
    public void after() {
    }

    @Test public void testThatGAVsCanBeResolved()
    {
        // make sure the resolver reads the correct config
        ResourceResolver<GAV> resolver = new StaticGAVResolver(config,session);
        GAV gav = GAV.fromString("org.apache.maven:apache-maven:3.3.3");
        Tree result = resolver.resolve( gav );
        assertTrue("Should not have branches",result.branches().length == 0);
        assertEquals( "b8eebb7ba265532bf55aa17f89440c2fe267bed3",result.fingerprint() );
    }


    @Test (expected = AutocodeException.class)
    public void testThatUnresolvableQueryWorks()
    {
        ResourceResolver<GAV> resolver = new StaticGAVResolver(config,session);
        resolver.resolve( GAV.fromString("org.apache.maven:notavailable:3.3.3") );
    }

    @Test public void testThatGAVsCanBeMaterialized() {
        // build tree:
        Tree input = null;
        ResourceMaterializer materializer = new DefaultMaterializer();
        Workspace workspace = mock(Workspace.class);
        materializer.get( input,workspace );
        // make sure workspace got the data:

    }
}
