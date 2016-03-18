package com.rebaze.mirror.api;

import java.net.URI;

import com.rebaze.stream.api.StreamSourceDTO;

public class ResourceDTO implements Comparable<ResourceDTO> {
	final StreamSourceDTO origin;
	final private String hash;
	final private URI uri;
	
	public ResourceDTO(StreamSourceDTO origin, URI uri, String hash) {
		this.origin = origin;
		this.uri = uri;
		this.hash = hash;
	}

	public StreamSourceDTO getOrigin() {
		return this.origin;
	}
	
	public String getHash() {
		return hash;
	}

	public URI getUri() {
		return uri;
	}

	@Override
	public int compareTo(ResourceDTO o) {
		// TODO Auto-generated method stub
		return 0;
	}	
	
}
