package com.rebaze.workspace.api;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.rebaze.mirror.api.ResourceDTO.HashType;

public class ResourceLinkTest {

	@Test
	public void testFormating() throws URISyntaxException {
		ResourceLink link = ResourceLink.from(new URI("streamlink:///SHA1/FOO"));
		assertEquals(HashType.SHA1,link.type());
		assertEquals("FOO",link.id());
	}
}
