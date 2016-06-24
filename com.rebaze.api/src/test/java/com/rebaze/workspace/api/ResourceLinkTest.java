package com.rebaze.workspace.api;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.rebaze.tree.api.HashAlgorithm;

public class ResourceLinkTest {

	@Test
	public void testFormating() throws URISyntaxException {
		ResourceLink link = ResourceLink.from(new URI("streamlink:///SHA1/FOO"));
		assertEquals(HashAlgorithm.SHA1,link.algorithm());
		assertEquals("FOO",link.fingerprint()); 
	}
}
