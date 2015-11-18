package com.rebaze.autocode.api.core;

import com.google.common.collect.Sets;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.config.JSonConfigBuilderTest;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.autocode.internal.transports.DefaultMaterializer;
import com.rebaze.autocode.internal.transports.ResourceTransporter;
import com.rebaze.autocode.internal.transports.StaticGAVResolver;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.core.util.DefaultTreeSessionFactory;
import com.rebaze.trees.core.util.TreeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.rebaze.trees.core.Selector.selector;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test (expected = AutocodeException.class)
    public void testMaterializeWithoutTransporters() {
        Tree input = mock(Tree.class);
        Set<ResourceTransporter> transporters = Sets.newHashSet();
        ResourceMaterializer materializer = new DefaultMaterializer(transporters);
        materializer.get( input );
    }

    @Test public void testThatGAVsCanBeMaterialized() {
        Tree input = mock(Tree.class);
        Set<ResourceTransporter> transporters = Sets.newHashSet();
        ResourceTransporter t1 = mock(ResourceTransporter.class);
        transporters.add(t1);
        File in = new File("dummy");
        when(t1.transport( input )).thenReturn( in );
        ResourceMaterializer materializer = new DefaultMaterializer(transporters);
        File f = materializer.get( input );
        assertNotNull(f);
        assertSame(in,f);

    }
}
