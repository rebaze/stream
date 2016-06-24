package com.rebaze.workspace.simple;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;

import org.junit.Test;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.tree.api.HashAlgorithm;

public class SimpleWorkspaceAdminTest {
	
	@Test
	public void testIndexFileWithChildRefs() throws Exception {
		SimpleWorkspaceAdmin w = new SimpleWorkspaceAdmin();
		StreamDefinitionDTO def = new StreamDefinitionDTO();
		def.localPath = "/tmp/";
		w.definition = def;
		
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.url = "http://rebaze.io/foo/index.xml";
		origin.name = "repo1";
		
		assertEquals(new File("/tmp/repo1/sub/a1.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://rebaze.io/foo/sub/a1.jar"),"",HashAlgorithm.MD5)));		
		assertEquals(new File("/tmp/repo1/a2.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://rebaze.io/foo/a2.jar"),"",HashAlgorithm.MD5)));
	}
	
	@Test
	public void testIndexFileWithChildRefsDirectRoot() throws Exception {
		SimpleWorkspaceAdmin w = new SimpleWorkspaceAdmin();
		StreamDefinitionDTO def = new StreamDefinitionDTO();
		def.localPath = "/tmp/";
		w.definition = def;
		
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.url = "http://rebaze.io/index.xml";
		origin.name = "repo1";
		
		assertEquals(new File("/tmp/repo1/sub/a1.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://rebaze.io/sub/a1.jar"),"",HashAlgorithm.MD5)));		
		assertEquals(new File("/tmp/repo1/a2.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://rebaze.io/a2.jar"),"",HashAlgorithm.MD5)));
	}
	
	@Test
	public void testDirIndexWithChildRefs() throws Exception {
		SimpleWorkspaceAdmin w = new SimpleWorkspaceAdmin();
		StreamDefinitionDTO def = new StreamDefinitionDTO();
		def.localPath = "/tmp/";
		w.definition = def;
		
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.url = "http://rebaze.io/p2";
		origin.name = "repo1";
		
		assertEquals(new File("/tmp/repo1/plugins/a1.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://rebaze.io/p2/plugins/a1.jar"),"",HashAlgorithm.MD5)));		
	}
	
	@Test
	public void testIndexFileWithExtdRefs() throws Exception {
		SimpleWorkspaceAdmin w = new SimpleWorkspaceAdmin();
		StreamDefinitionDTO def = new StreamDefinitionDTO();
		def.localPath = "/tmp/";
		w.definition = def;
		
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.url = "http://rebaze.io/foo/index.xml";
		origin.name = "repo1";
		
		assertEquals(new File("/tmp/repo1/other/sub/a1.jar"),w.getPathFor(new ResourceDTO(origin,new URI("http://apache.org/other/sub/a1.jar"),"",HashAlgorithm.MD5)));		
	}
}
