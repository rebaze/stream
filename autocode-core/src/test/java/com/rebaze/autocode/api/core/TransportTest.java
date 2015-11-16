package com.rebaze.autocode.api.core;

import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.api.transport.Workspace;
import com.rebaze.autocode.internal.transports.DefaultMaterializer;
import com.rebaze.autocode.internal.transports.StaticGAVResolver;
import com.rebaze.commons.tree.Tree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by tonit on 16/11/15.
 */
public class TransportTest
{
    @Test public void testThatGAVsCanBeResolved() {
        // make sure the resolver reads the correct config

        ResourceResolver<GAV> resolver = new StaticGAVResolver(null);
        GAV gav = GAV.fromString("");
        Tree result = resolver.resolve( gav );
        assertTrue("Should not have branches",result.branches().length == 0);
        assertEquals( "",result.fingerprint() );
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
