package com.rebaze.repository.p2;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.repository.p2.P2MirrorAdapter;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;

public class P2MirrorAdapterTest {
	
	@Test
	public void simpleTest() {
		StreamDefinitionDTO def = new StreamDefinitionDTO();
		def.sources = new StreamSourceDTO[] {
				stream("file:///Users/tonit/Desktop/jenkinshome/mirror")
		};
		P2MirrorAdapter mirror = new P2MirrorAdapter();
		
		List<ResourceDTO> resources = mirror.fetchResources();
		assertEquals(22927,resources.size());
		
	}
	
	@Test
	public void simpleParserTest() {
		P2MirrorAdapter mirror = new P2MirrorAdapter();
		assertEquals("toni/foo",mirror.simpleReplace("${name}/foo", "name", "toni"));
	}

	private StreamSourceDTO stream(String uri) {
		StreamSourceDTO src = new StreamSourceDTO();
		src.active = true;
		src.name = "test";
		src.type = P2MirrorAdapter.TYPE;
		src.url = uri;
		return src;
	}
}
